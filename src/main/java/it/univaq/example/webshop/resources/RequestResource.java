package it.univaq.example.webshop.resources;

import jakarta.ws.rs.Consumes;
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
import it.univaq.example.webshop.business.RequestResourceDB;
import it.univaq.example.webshop.model.Request;
import it.univaq.framework.exceptions.RESTWebApplicationException;
import it.univaq.framework.security.Logged;

@Path("richiesta")
public class RequestResource {

    public static Request createRequest() {
        return new Request();
    }

    @Logged
    @GET
    @Path("{id: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRequest(@PathParam("id") int request_key) throws RESTWebApplicationException {
        Request l = RequestResourceDB.getRequest(request_key);
        return Response.ok(l).build();
    }   
    
    @Logged
    @PUT
    @Consumes({"application/json"})
    public Response setRequest(Request request, @Context SecurityContext securityContext) throws RESTWebApplicationException {
        try {
            RequestResourceDB.setRequest(request);
            return Response.noContent().build();
        } catch (NotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity("Request not found").build();
        } catch (RESTWebApplicationException ex) {
            return Response.serverError()
                    .entity(ex.getMessage()) //NEVER IN PRODUCTION!
                    .build();
        }
    }
}