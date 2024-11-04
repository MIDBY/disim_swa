package it.univaq.example.webshop.resources;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
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

import java.util.List;

import it.univaq.example.webshop.business.RequestCharacteristicResourceDB;
import it.univaq.example.webshop.model.RequestCharacteristic;
import it.univaq.framework.data.DataException;
import it.univaq.framework.exceptions.RESTWebApplicationException;
import it.univaq.framework.security.Logged;

public class RequestCharacteristicsResource {

    private final List<RequestCharacteristic> characteristics;

    RequestCharacteristicsResource(List<RequestCharacteristic> characteristics) {
        this.characteristics = characteristics;
    }

    @Logged
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRequestCharacteristicsByRequest() throws RESTWebApplicationException {
        if(characteristics.size() > 0)
            return Response.ok(characteristics).build();
        else
            return Response.status(Response.Status.NOT_FOUND).entity("Nessuna caratteristica richiesta trovata").build();
    }

    @Logged
    @GET
    @Path("{id: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRequestCharacteristic(@PathParam("id") int requestCharacteristic_key, @Context UriInfo uriinfo) throws RESTWebApplicationException {
        RequestCharacteristic rc = getSingleRequestCharacteristic(requestCharacteristic_key);
        if(rc != null)
            return Response.ok(rc).build();
        else
            return Response.status(Response.Status.NOT_FOUND).entity("Caratteristica richiesta non trovata").build();
    }   
    
    @Logged
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setRequestCharacteristic(RequestCharacteristic requestCharacteristic, @Context ContainerRequestContext req) throws RESTWebApplicationException {
        int user_key = Integer.parseInt(req.getProperty("userid").toString());
        if(user_key == requestCharacteristic.getRequest().getOrdering().getKey()) {
            try {
                RequestCharacteristicResourceDB.setRequestCharacteristic(requestCharacteristic);
                return Response.noContent().build();
            } catch (NotFoundException ex) {
                return Response.status(Response.Status.NOT_FOUND).entity("Caratteristica richiesta non trovata").build();
            } catch (RESTWebApplicationException ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Errore generico").build();
            } 
        } else
            return Response.status(Response.Status.BAD_REQUEST).entity("Utente non autorizzato").build();
    }

    @Logged
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setRequestCharacteristics(List<RequestCharacteristic> requestCharacteristics, @Context ContainerRequestContext req) throws RESTWebApplicationException {
        int user_key = Integer.parseInt(req.getProperty("userid").toString());
        if(user_key == requestCharacteristics.get(0).getRequest().getOrdering().getKey()) {
            try {
                for(RequestCharacteristic rc : requestCharacteristics)
                    RequestCharacteristicResourceDB.setRequestCharacteristic(rc);
                return Response.noContent().build();
            } catch (NotFoundException ex) {
                return Response.status(Response.Status.NOT_FOUND).entity("Caratteristica richiesta non trovata").build();
            } catch (RESTWebApplicationException ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Errore generico").build();
            }
        } else
            return Response.status(Response.Status.BAD_REQUEST).entity("Utente non autorizzato").build();
    }

    @Logged
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteRequestCharacteristic(@QueryParam("id") int requestCharacteristic_key, @Context ContainerRequestContext req) throws RESTWebApplicationException {
        RequestCharacteristic rc = getSingleRequestCharacteristic(requestCharacteristic_key);
        if(rc != null) {
            int user_key = Integer.parseInt(req.getProperty("userid").toString());
            if(user_key == rc.getRequest().getOrdering().getKey()) {
                try {
                    RequestCharacteristicResourceDB.deleteRequestCharacteristic(rc);
                    return Response.noContent().build();
                } catch (NotFoundException ex) {
                    return Response.status(Response.Status.NOT_FOUND).entity("Caratteristica richiesta non trovata").build();
                } catch (RESTWebApplicationException | DataException ex) {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Errore generico").build();
                }
            } else
                return Response.status(Response.Status.BAD_REQUEST).entity("Utente non autorizzato").build();    
        } else
            return Response.status(Response.Status.NOT_FOUND).entity("Caratteristica richiesta non trovata").build();
    }

    private RequestCharacteristic getSingleRequestCharacteristic(int requestCharacteristic_key) {
        for(RequestCharacteristic rc : characteristics)
            if(rc.getKey() == requestCharacteristic_key)
                return rc;
        return null;
    }
}