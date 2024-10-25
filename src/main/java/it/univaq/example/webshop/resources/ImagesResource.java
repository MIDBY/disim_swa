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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.File;
import java.util.List;

import it.univaq.example.webshop.business.ImageResourceDB;
import it.univaq.example.webshop.model.Image;
import it.univaq.example.webshop.model.UserRoleEnum;
import it.univaq.framework.data.DataException;
import it.univaq.framework.exceptions.RESTWebApplicationException;
import it.univaq.framework.security.Logged;

@Path("immagini")
public class ImagesResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getImages() throws RESTWebApplicationException {
        List<Image> images = ImageResourceDB.getImages();
        if(images.size() > 0)
            return Response.ok(images).build();
        else
            return Response.status(Response.Status.NOT_FOUND).entity("Nessuna immagine trovata").build();
    }

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
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setImage(Image image, @Context ContainerRequestContext req) throws RESTWebApplicationException {
        if(req.getSecurityContext().isUserInRole(UserRoleEnum.AMMINISTRATORE.toString())) {
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
                return Response.status(Response.Status.NOT_FOUND).entity("Image not found").build();
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
        String path = sc.getInitParameter("images.directory");
		File file = new File(path + "\\" + image.getFilename());
        return Response
                .ok((Object) file)
                .header("content-disposition", "attachment; filename="+image.getFilename())
                .build();
    }
}