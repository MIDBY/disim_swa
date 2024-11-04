package it.univaq.example.webshop.resources;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServletRequest;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
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
    @Produces(MediaType.APPLICATION_JSON)
    public Response setImage(
                            @FormDataParam("immagine") InputStream uploaded, @FormDataParam("immagine") FormDataContentDisposition file_detail,
                            @FormDataParam("titolo") String titolo, @Context HttpServletRequest request,
                            @Context ContainerRequestContext req, @Context ServletContext sc) throws RESTWebApplicationException, ServletException {
        if(req.getSecurityContext().isUserInRole(UserRoleEnum.AMMINISTRATORE.toString())) {
            try {
                if (file_detail == null || file_detail.getFileName() == null || file_detail.getFileName().isEmpty()) {
                    return Response.status(Response.Status.NOT_FOUND).entity("Immagine non trovata").build();
                }

                Image image = new Image();
                image.setFilename(AuthHelpers.sanitizeFilename(file_detail.getFileName()));
                image.setImageType("image/" + image.getFilename().split("\\.")[1]);
                image.setImageSize(file_detail.getSize());
                image.setCaption(titolo + " image");
                if(image.getImageSize() > 0 || !file_detail.getFileName().isEmpty()) {
                    java.nio.file.Path target = Paths.get(sc.getInitParameter("images.directory") + File.separator + image.getFilename());
                    // save it
                    int guess = 0;
                    while(Files.exists(target, LinkOption.NOFOLLOW_LINKS))
                        target = Paths.get(sc.getInitParameter("images.directory") + File.separator + (++guess) + "_" + image.getFilename());
                    try {
                        OutputStream out = null;
                        int read = 0;
                        byte[] bytes = new byte[1024];
                        long fileSize = 0;

                        out = new FileOutputStream(new File(target.toString()));
                        while ((read = uploaded.read(bytes)) != -1) {
                            out.write(bytes, 0, read);
                            fileSize += read;
                        }
                        out.flush();
                        out.close();

                        if(fileSize > image.getImageSize())
                            image.setImageSize(fileSize);

                    } catch (IOException e) {
                        return Response.status(Response.Status.CONFLICT).entity("Caricamento file fallito: " + e.getMessage()).build();
                    }
                }
                image = ImageResourceDB.setImage(image);
                return Response.ok(image).build();
            } catch (NotFoundException ex) {
                return Response.status(Response.Status.NOT_FOUND).entity("Immagine non trovata").build();
            } catch (RESTWebApplicationException ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Errore generico").build();
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
                ImageResourceDB.deleteImage(ImageResourceDB.getImage(image_key).getKey());
                return Response.noContent().build();
            } catch (NotFoundException ex) {
                return Response.status(Response.Status.NOT_FOUND).entity("Immagine non trovata").build();
            } catch (RESTWebApplicationException | DataException ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Errore generico").build();
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