package it.univaq.example.webshop.resources;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import it.univaq.example.webshop.business.ImageResourceDB;
import it.univaq.example.webshop.model.Image;
import it.univaq.example.webshop.model.UserRoleEnum;
import it.univaq.framework.data.DataException;
import it.univaq.framework.exceptions.RESTWebApplicationException;
import it.univaq.framework.security.AuthHelpers;
import it.univaq.framework.security.Logged;

@Path("immagini")
@MultipartConfig(
    maxFileSize = 20848820,
    maxRequestSize = 418018841
)
public class ImagesResource {

    @Logged
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getImages() throws RESTWebApplicationException {
        List<Image> images = ImageResourceDB.getImages();
        if(images.size() > 0)
            return Response.ok(images).build();
        else
            return Response.status(Response.Status.NOT_FOUND).entity("Nessuna immagine trovata").build();
    }

    @Logged
    @GET
    @Path("{id: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getImage(@PathParam("id") int image_key) throws RESTWebApplicationException {
        Image image = ImageResourceDB.getImage(image_key);
        if(image != null)
            return Response.ok(image).build();
        else
            return Response.status(Response.Status.NOT_FOUND).entity("Nessuna immagine trovata").build();
    } 
    
    @Logged
    @GET
    @Path("categoria")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getImageByCategory(@QueryParam("id") int category_key) throws RESTWebApplicationException {
        Image image = ImageResourceDB.getImageByCategory(category_key);
        if(image != null)
            return Response.ok(image).build();
        else
            return Response.status(Response.Status.NOT_FOUND).entity("Nessuna immagine trovata").build();
    }   
    
    @Logged
    @PUT
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response setImage(
                            @FormDataParam("immagine") InputStream uploaded, @FormDataParam("immagine") FormDataContentDisposition file_detail,
                            @Context HttpServletRequest request,
                            @Context ContainerRequestContext req, @Context ServletContext sc) throws RESTWebApplicationException, ServletException {
        if(req.getSecurityContext().isUserInRole(UserRoleEnum.AMMINISTRATORE.toString())) {
            try {
                Part file_da_caricare = request.getPart("immagine");
                if(file_da_caricare != null && file_da_caricare.getSubmittedFileName() != "") {
                    Image image = new Image();
                    
                    image.setFilename(AuthHelpers.sanitizeFilename(((Part) file_da_caricare).getSubmittedFileName()));
                    image.setImageType(((Part) file_da_caricare).getContentType());
                    image.setImageSize(((Part) file_da_caricare).getSize());
                    image.setCaption("Image");
                    if(image.getImageSize() > 0 && !image.getFilename().isEmpty()) {
                        java.nio.file.Path target = Paths.get(sc.getInitParameter("images.directory") + File.separator + image.getFilename());
                        int guess = 0;
                        while(Files.exists(target, LinkOption.NOFOLLOW_LINKS))
                            target = Paths.get(sc.getInitParameter("images.directory") + File.separator + (++guess) + "_" + image.getFilename());
                        try (InputStream temp_upload = ((Part) file_da_caricare).getInputStream()) {
                            Files.copy(temp_upload, target, StandardCopyOption.REPLACE_EXISTING);
                        }
                        image.setImageData(((Part) file_da_caricare).getInputStream());
                    }
                    image = ImageResourceDB.setImage(image);
                    return Response.ok(image.getKey()).build();
                } else
                    return Response.status(Response.Status.NOT_FOUND).entity("Immagine non trovata").build();
            } catch (NotFoundException | DataException | IOException ex) {
                return Response.status(Response.Status.NOT_FOUND).entity("Immagine non trovata").build();
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
    public Response deleteImage(@QueryParam("id") int image_key, @Context ContainerRequestContext req) throws RESTWebApplicationException {
        if(req.getSecurityContext().isUserInRole(UserRoleEnum.AMMINISTRATORE.toString())) {
            try {
                ImageResourceDB.deleteImage(ImageResourceDB.getImage(image_key));
                return Response.noContent().build();
            } catch (NotFoundException ex) {
                return Response.status(Response.Status.NOT_FOUND).entity("Immagine non trovata").build();
            } catch (RESTWebApplicationException | DataException ex) {
                return Response.serverError()
                        .entity(ex.getMessage()) //NEVER IN PRODUCTION!
                        .build();
            }
        } else 
            return Response.status(Response.Status.BAD_REQUEST).entity("Non sei l'amministratore").build();
    }

    @GET    
    @Path("download")
    @Produces({"image/jpeg", "image/png"})
    public Response download(@QueryParam("id") int image_key, @Context ServletContext sc) {
        Image image = ImageResourceDB.getImage(image_key);
        if(image != null) {
            String path = sc.getInitParameter("images.directory");
            File file = new File(path + "\\" + image.getFilename());
            return Response
                    .ok((Object) file)
                    .header("content-disposition", "attachment; filename="+image.getFilename())
                    .build();
        } else
            return Response.status(Response.Status.NOT_FOUND).entity("Immagine non trovata").build();
    }
}