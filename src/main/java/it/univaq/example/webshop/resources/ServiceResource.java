package it.univaq.example.webshop.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

import it.univaq.example.webshop.business.ServiceResourceDB;
import it.univaq.example.webshop.model.Service;
import it.univaq.framework.exceptions.RESTWebApplicationException;
import it.univaq.framework.security.Logged;

@Path("servizio")
public class ServiceResource {

    public static Service createService() {
        return new Service();
    }

    @Logged
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getServices() throws RESTWebApplicationException {
        List<Service> l = ServiceResourceDB.getServices();
        return Response.ok(l).build();
    }

    @Logged
    @GET
    @Path("{id: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getService(@PathParam("id") int service_key) throws RESTWebApplicationException {
        Service l = ServiceResourceDB.getService(service_key);
        return Response.ok(l).build();
    }    
    
    @Logged
    @GET
    @Path("{script: [a-zA-Z0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getServiceByScript(@PathParam("script") String script) throws RESTWebApplicationException {
        Service l = ServiceResourceDB.getServiceByScript(script);
        return Response.ok(l).build();
    }

    @Logged
    @GET
    @Path("{idgruppo: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getServicesByGroup(@PathParam("idgruppo") int group_key) throws RESTWebApplicationException {
        List<Service> result = ServiceResourceDB.getServicesByGroup(group_key);
        return Response.ok(result).build();
    }
}