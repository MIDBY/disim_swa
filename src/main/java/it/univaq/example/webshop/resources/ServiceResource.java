package it.univaq.example.webshop.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

import it.univaq.example.webshop.business.GroupResourceDB;
import it.univaq.example.webshop.business.ServiceResourceDB;
import it.univaq.example.webshop.model.Service;
import it.univaq.framework.exceptions.RESTWebApplicationException;
import it.univaq.framework.security.Logged;

public class ServiceResource {

    private List<Service> services;

    private void setService(int userId) {
        this.services = ServiceResourceDB.getServicesByGroup(GroupResourceDB.getGroupByUser(userId).getKey());
    }

    @Logged
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getServices(@Context ContainerRequestContext req) throws RESTWebApplicationException {
        setService(Integer.parseInt(req.getProperty("userid").toString()));
        return Response.ok(services).build();
    }

    @Logged
    @GET
    @Path("{id: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getServiceById(@PathParam("id") int service_key, @Context ContainerRequestContext req) throws RESTWebApplicationException {
        setService(Integer.parseInt(req.getProperty("userid").toString()));
        Service result = getSingleService(service_key, null);
        if(result != null)
            return Response.ok(result).build();
        else
            return Response.status(Response.Status.NOT_FOUND).entity("Nessun servizio trovato").build();
    }    
    
    @Logged
    @GET
    @Path("{script: [a-zA-Z0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getServiceByScript(@PathParam("script") String script, @Context ContainerRequestContext req) throws RESTWebApplicationException {
        setService(Integer.parseInt(req.getProperty("userid").toString()));
        Service result = getSingleService(0, script);
        if(result != null)    
            return Response.ok(result).build();
        else
            return Response.status(Response.Status.NOT_FOUND).entity("Nessun servizio trovato").build();
    }

    private Service getSingleService(int id, String script) {
        for(Service s : services) {
            if(id > 0 && s.getKey() == id)    
                return s;
            if(!script.isEmpty() && s.getScript().equals(script))
                return s;    
        }
        return null;
    }
}