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
import it.univaq.example.webshop.business.RequestCharacteristicResourceDB;
import it.univaq.example.webshop.model.RequestCharacteristic;
import it.univaq.framework.data.DataException;
import it.univaq.framework.exceptions.RESTWebApplicationException;
import it.univaq.framework.security.Logged;

public class RequestCharacteristicResource {

    public static RequestCharacteristic createRequestCharacteristic() {
        return new RequestCharacteristic();
    }

    @Logged
    @GET
    @Path("{id: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRequestCharacteristic(@PathParam("id") int requestCharacteristic_key) throws RESTWebApplicationException {
        RequestCharacteristic l = RequestCharacteristicResourceDB.getRequestCharacteristic(requestCharacteristic_key);
        return Response.ok(l).build();
    }   
    
    @Logged
    @PUT
    @Consumes({"application/json"})
    public Response setRequestCharacteristic(RequestCharacteristic requestCharacteristic, @Context SecurityContext securityContext) throws RESTWebApplicationException {
        try {
            RequestCharacteristicResourceDB.setRequestCharacteristic(requestCharacteristic);
            return Response.noContent().build();
        } catch (NotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity("Request characteristic not found").build();
        } catch (RESTWebApplicationException ex) {
            return Response.serverError()
                    .entity(ex.getMessage()) //NEVER IN PRODUCTION!
                    .build();
        }
    }

    @Logged
    @DELETE
    @Consumes({"application/json"})
    public Response deleteRequestCharacteristic(RequestCharacteristic requestCharacteristic, @Context SecurityContext securityContext) throws RESTWebApplicationException {
        try {
            RequestCharacteristicResourceDB.deleteRequestCharacteristic(requestCharacteristic);
            return Response.noContent().build();
        } catch (NotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity("Request characteristic not found").build();
        } catch (RESTWebApplicationException | DataException ex) {
            return Response.serverError()
                    .entity(ex.getMessage()) //NEVER IN PRODUCTION!
                    .build();
        }
    }
}