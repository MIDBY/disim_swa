package it.univaq.example.webshop.resources;

import jakarta.servlet.ServletContext;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.io.File;
import it.univaq.example.webshop.business.ImageResourceDB;
import it.univaq.example.webshop.model.Image;
import it.univaq.framework.data.DataException;
import it.univaq.framework.exceptions.RESTWebApplicationException;
import it.univaq.framework.security.Logged;

@Path("immagine")
public class ImageResource {

    public static Image createImage() {
        return new Image();
    }

    @Logged
    @GET
    @Path("{id: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getImage(@PathParam("id") int image_key) throws RESTWebApplicationException {
        Image l = ImageResourceDB.getImage(image_key);
        return Response.ok(l).build();
    } 
    
    @Logged
    @GET
    @Path("categoria/{id: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getImageByCategory(@PathParam("id") int category_key) throws RESTWebApplicationException {
        Image l = ImageResourceDB.getImageByCategory(category_key);
        return Response.ok(l).build();
    }   
    
    @Logged
    @PUT
    @Consumes({"application/json"})
    public Response setImage(Image image, @Context SecurityContext securityContext) throws RESTWebApplicationException {
        try {
            ImageResourceDB.setImage(image);
            return Response.noContent().build();
        } catch (NotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity("Image not found").build();
        } catch (RESTWebApplicationException ex) {
            return Response.serverError()
                    .entity(ex.getMessage()) //NEVER IN PRODUCTION!
                    .build();
        }
    }

    @Logged
    @DELETE
    @Consumes({"application/json"})
    public Response deleteImage(Image image, @Context SecurityContext securityContext) throws RESTWebApplicationException {
        try {
            ImageResourceDB.deleteImage(image);
            return Response.noContent().build();
        } catch (NotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity("Image not found").build();
        } catch (RESTWebApplicationException | DataException ex) {
            return Response.serverError()
                    .entity(ex.getMessage()) //NEVER IN PRODUCTION!
                    .build();
        }
    }

    @GET    
    @Path("download/{id: [0-9]+}")
    @Produces({"image/jpeg", "image/png"})
    public Response download(@PathParam("id") int image_key, @Context ServletContext sc) {
        Image image = ImageResourceDB.getImage(image_key);
        String path = sc.getInitParameter("images.directory");
		File file = new File(path + "\\" + image.getFilename());
        return Response
                .ok((Object) file)
                .header("content-disposition", "attachment; filename="+image.getFilename())
                .build();
    }
}