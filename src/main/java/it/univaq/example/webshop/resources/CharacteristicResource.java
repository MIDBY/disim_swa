package it.univaq.example.webshop.resources;

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
import it.univaq.example.webshop.business.CharacteristicResourceDB;
import it.univaq.example.webshop.model.Characteristic;
import it.univaq.framework.data.DataException;
import it.univaq.framework.exceptions.RESTWebApplicationException;
import it.univaq.framework.security.Logged;

@Path("caratteristica")
public class CharacteristicResource {

    public static Characteristic createCharacteristic() {
        return new Characteristic();
    }

    @Logged
    @GET
    @Path("{id: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCharacteristic(@PathParam("id") int characteristic_key) throws RESTWebApplicationException {
        Characteristic l = CharacteristicResourceDB.getCharacteristic(characteristic_key);
        return Response.ok(l).build();
    }   
    
    @Logged
    @PUT
    @Consumes({"application/json"})
    public Response setCharacteristic(Characteristic characteristic, @Context SecurityContext securityContext) throws RESTWebApplicationException {
        try {
            CharacteristicResourceDB.setCharacteristic(characteristic);
            return Response.noContent().build();
        } catch (NotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity("Characteristic not found").build();
        } catch (RESTWebApplicationException ex) {
            return Response.serverError()
                    .entity(ex.getMessage()) //NEVER IN PRODUCTION!
                    .build();
        }
    }

    @Logged
    @DELETE
    @Consumes({"application/json"})
    public Response deleteCharacteristic(Characteristic characteristic, @Context SecurityContext securityContext) throws RESTWebApplicationException {
        try {
            CharacteristicResourceDB.deleteCharacteristic(characteristic);
            return Response.noContent().build();
        } catch (NotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity("Characteristic not found").build();
        } catch (RESTWebApplicationException | DataException ex) {
            return Response.serverError()
                    .entity(ex.getMessage()) //NEVER IN PRODUCTION!
                    .build();
        }
    }
}