package it.univaq.example.webshop.resources;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Part;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.glassfish.jersey.media.multipart.FormDataParam;

import it.univaq.example.webshop.business.CategoryResourceDB;
import it.univaq.example.webshop.business.ImageResourceDB;
import it.univaq.example.webshop.model.Category;
import it.univaq.example.webshop.model.Image;
import it.univaq.example.webshop.model.UserRoleEnum;
import it.univaq.framework.data.DataException;
import it.univaq.framework.exceptions.RESTWebApplicationException;
import it.univaq.framework.security.AuthHelpers;
import it.univaq.framework.security.Logged;

@Path("categorie")
public class CategoriesResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCategories(@Context UriInfo uriinfo) throws RESTWebApplicationException {
        List<Category> categories =  CategoryResourceDB.getCategoriesByDeleted(false);
        List<Map<String,Object>> result = new ArrayList<>();
        for(Category c : categories) {
            Map<String, Object> e = new LinkedHashMap<>();
            e.put("id", c.getKey());
            e.put("nome", c.getName());
            e.put("categoria_padre", c.getFatherCategory());
            URI uri = uriinfo.getBaseUriBuilder()
                .path(CategoriesResource.class)
                .path(CategoriesResource.class, "getCategory")
                .build(c.getKey());
            e.put("caratteristiche", uri);
            URI uri2 = uriinfo.getBaseUriBuilder()
            .path(ImagesResource.class)
            .path(ImagesResource.class, "getImageByCategory")
            .queryParam("id", c.getKey())
            .build();
            e.put("immagine", uri2);
            e.put("versione", c.getVersion());
            result.add(e);
        }
        if(result.size() > 0)
            return Response.ok(result).build();
        else
            return Response.status(Response.Status.NOT_FOUND).entity("Nessuna categoria trovata").build();
    }

    @GET
    @Path("genitori")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFatherCategories() throws RESTWebApplicationException {
        List<Category> result = CategoryResourceDB.getFatherCategories();
        if(result.size() > 0)
            return Response.ok(result).build();
        else
            return Response.status(Response.Status.NOT_FOUND).entity("Nessuna categoria trovata").build();
    }
/*
    @GET
    @Path("figlidi/{id: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCategoriesSonsOf(@PathParam("id") int category_key) throws RESTWebApplicationException {
        List<Category> result = CategoryResourceDB.getCategoriesSonsOf(category_key);
        return Response.ok(result).build();
    } */

    @GET
    @Path("piuvendute")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMostSoldCategories() throws RESTWebApplicationException {
        List<Category> result = CategoryResourceDB.getMostSoldCategories();
        if(result.size() > 0)
            return Response.ok(result).build();
        else
            return Response.status(Response.Status.NOT_FOUND).entity("Nessuna categoria trovata").build();
    }

    @Path("{id: [0-9]+}")
    public CharacteristicsResource getCategory(@PathParam("id") int category_key) throws RESTWebApplicationException {
        Category category = CategoryResourceDB.getCategory(category_key);
        return new CharacteristicsResource(category);
    }   
    
    @Logged
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setCategory(Category category, @Context ContainerRequestContext req) throws RESTWebApplicationException {
        if(req.getSecurityContext().isUserInRole(UserRoleEnum.AMMINISTRATORE.toString())) {
            try {
                Category c = CategoryResourceDB.getCategory(category.getKey());
                category.setVersion(c.getVersion());
                if(category.getFatherCategory() != null && category.getFatherCategory().getKey() == null)
                    category.setFatherCategoryNull();
                if(category.getKey() == 24)
                    category.setDeleted(c.isDeleted());
                CategoryResourceDB.setCategory(category);
                return Response.noContent().build();
            } catch (NotFoundException ex) {
                return Response.status(Response.Status.NOT_FOUND).entity("Category not found").build();
            } catch (RESTWebApplicationException ex) {
                return Response.serverError()
                        .entity(ex.getMessage()) //NEVER IN PRODUCTION!
                        .build();
            }
        } else 
            return Response.status(Response.Status.BAD_REQUEST).entity("Non sei l'amministratore").build();
    }

    @Logged
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.MULTIPART_FORM_DATA})
    public Response editCategory(@FormParam("id") int id, @FormParam("nome") String nome, @FormParam("idCategoriaPadre") int idCategoriaPadre,
                                @FormDataParam("immagine") Part file_da_caricare, @Context ContainerRequestContext req, @Context ServletContext sc) throws RESTWebApplicationException, IOException {
        if(req.getSecurityContext().isUserInRole(UserRoleEnum.AMMINISTRATORE.toString())) {
            try {
                Category category = CategoryResourceDB.getCategory(id);
                if(category == null)
                    category = new Category();
                category.setName(nome);
                if(idCategoriaPadre == 0)
                    category.setFatherCategoryNull();
                else
                    category.setFatherCategory(CategoryResourceDB.getCategory(idCategoriaPadre));

                if(file_da_caricare.getSubmittedFileName() != "") {
                    Image image;
                    if(category.getImage() != null)
                        image = category.getImage();
                    else
                        image = new Image();
                    
                    image.setFilename(AuthHelpers.sanitizeFilename(file_da_caricare.getSubmittedFileName()));
                    image.setImageType(file_da_caricare.getContentType());
                    image.setImageSize(file_da_caricare.getSize());
                    image.setCaption(category.getName() + " image");
                    if(image.getImageSize() > 0 && !image.getFilename().isEmpty()) {
                        java.nio.file.Path target = Paths.get(sc.getInitParameter("images.directory") + File.separator + image.getFilename());
                        int guess = 0;
                        while(Files.exists(target, LinkOption.NOFOLLOW_LINKS))
                            target = Paths.get(sc.getInitParameter("images.directory") + File.separator + (++guess) + "_" + image.getFilename());
                        try (InputStream temp_upload = file_da_caricare.getInputStream()) {
                            Files.copy(temp_upload, target, StandardCopyOption.REPLACE_EXISTING);
                        }
                        image.setImageData(file_da_caricare.getInputStream());
                    }
                    ImageResourceDB.setImage(image);
                    category.setImage(image);
                }
                CategoryResourceDB.setCategory(category);
                return Response.noContent().build();
            } catch(DataException ex) {
                return Response.status(Response.Status.NOT_FOUND).entity("Category not updated").build();
            } catch (RESTWebApplicationException ex) {
                return Response.serverError()
                        .entity(ex.getMessage()) //NEVER IN PRODUCTION!
                        .build();
            }
        } else 
            return Response.status(Response.Status.BAD_REQUEST).entity("Non sei l'amministratore").build();
    }

    @Logged
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteCategory(@QueryParam("id") int category_key, @Context ContainerRequestContext req) throws RESTWebApplicationException {
        if(req.getSecurityContext().isUserInRole(UserRoleEnum.AMMINISTRATORE.toString())) {
            try {
                CategoryResourceDB.deleteCategory(category_key);
                return Response.noContent().build();
            } catch (NotFoundException ex) {
                return Response.status(Response.Status.NOT_FOUND).entity("Category not found").build();
            } catch (RESTWebApplicationException | DataException ex) {
                return Response.serverError()
                        .entity(ex.getMessage()) //NEVER IN PRODUCTION!
                        .build();
            }
        } else 
            return Response.status(Response.Status.BAD_REQUEST).entity("Non sei l'amministratore").build();
    }
}