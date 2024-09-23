package it.univaq.example.webshop.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import it.univaq.example.webshop.business.CharacteristicResourceDB;
import it.univaq.example.webshop.model.Characteristic;
import it.univaq.framework.exceptions.RESTWebApplicationException;
import it.univaq.framework.security.Logged;

@Path("caratteristiche")
public class CharacteristicsResource {

    @Logged
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCharacteristics() throws RESTWebApplicationException {
        List<Characteristic> l =  CharacteristicResourceDB.getCharacteristics();
        return Response.ok(l).build();
    }

    @Logged
    @GET
    @Path("categoria/{id: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCharacteristicsByCategory(@PathParam("id") int category_key) throws RESTWebApplicationException {
        List<Characteristic> result = CharacteristicResourceDB.getCharacteristicsByCategory(category_key);
        return Response.ok(result).build();
    }
}