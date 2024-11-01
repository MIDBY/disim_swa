package it.univaq.example.webshop.resources;

import jakarta.servlet.ServletContext;
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

import java.io.IOException;
import java.util.List;

import it.univaq.example.webshop.business.CategoryResourceDB;
import it.univaq.example.webshop.business.CharacteristicResourceDB;
import it.univaq.example.webshop.business.ImageResourceDB;
import it.univaq.example.webshop.model.Category;
import it.univaq.example.webshop.model.Characteristic;
import it.univaq.example.webshop.model.Image;
import it.univaq.example.webshop.model.UserRoleEnum;
import it.univaq.framework.data.DataException;
import it.univaq.framework.exceptions.RESTWebApplicationException;
import it.univaq.framework.security.Logged;

@Path("categorie")
public class CategoriesResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCategories(@Context UriInfo uriinfo) throws RESTWebApplicationException {
        List<Category> categories =  CategoryResourceDB.getCategoriesByDeleted(false);
        for(Category c : categories) {
            c.setCharacteristics(CharacteristicResourceDB.getCharacteristicsByCategory(c.getKey()));
        }
        if(categories.size() > 0)
            return Response.ok(categories).build();
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
                return Response.status(Response.Status.NOT_FOUND).entity("Categoria non trovata").build();
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
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response editCategory(@FormParam("id") int id, @FormParam("nome") String nome, @FormParam("idCategoriaPadre") int idCategoriaPadre,
                                @FormParam("immagine") int immagine, 
                                @FormParam("caratteristiche") String characteristics, @Context ContainerRequestContext req, @Context ServletContext sc) throws RESTWebApplicationException, IOException {
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

                if(immagine > 0) {
                    Image image = ImageResourceDB.getImage(immagine);
                    category.setImage(image);
                }
                if(!characteristics.isBlank()) {
                    String[] parts = characteristics.split("§");
                    for(String s : parts){
                        String[] sub = s.split("ç");

                        Characteristic characteristic = CharacteristicResourceDB.getCharacteristic(Integer.parseInt(sub[0]));
                        if(characteristic == null) {
                            characteristic = new Characteristic();
                            characteristic.setKey(Integer.parseInt(sub[0]));
                        }
                        characteristic.setName(sub[1]);
                        if(sub[2] != "0")
                            characteristic.setCategory(CategoryResourceDB.getCategory(Integer.parseInt(sub[2])));
                        else
                            characteristic.setCategory(null);
                        if(!sub[3].contains("Indifferent") && !sub[3].contains("indifferent"))
                            sub[3] += ",Indifferent"; 
                        characteristic.setDefaultValues(sub[3]);
                        
                        CharacteristicResourceDB.setCharacteristic(characteristic);
                    }
                }
                CategoryResourceDB.setCategory(category);
                return Response.noContent().build();
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
                return Response.status(Response.Status.NOT_FOUND).entity("Categoria non trovata").build();
            } catch (RESTWebApplicationException | DataException ex) {
                return Response.serverError()
                        .entity(ex.getMessage()) //NEVER IN PRODUCTION!
                        .build();
            }
        } else 
            return Response.status(Response.Status.BAD_REQUEST).entity("Non sei l'amministratore").build();
    }
}