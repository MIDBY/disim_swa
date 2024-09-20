package it.univaq.example.webshop.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import it.univaq.example.webshop.business.ImageResourceDB;
import it.univaq.example.webshop.model.Image;
import it.univaq.framework.exceptions.RESTWebApplicationException;
import it.univaq.framework.security.Logged;

@Path("immagini")
public class ImagesResource {

    @Logged
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() throws RESTWebApplicationException {
        List<Image> l = ImageResourceDB.getAll();
        return Response.ok(l).build();
    }
}