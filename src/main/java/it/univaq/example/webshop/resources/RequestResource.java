package it.univaq.example.webshop.resources;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import it.univaq.example.webshop.business.ProposalResourceDB;
import it.univaq.example.webshop.business.RequestCharacteristicResourceDB;
import it.univaq.example.webshop.business.RequestResourceDB;
import it.univaq.example.webshop.model.Request;
import it.univaq.example.webshop.model.UserRoleEnum;
import it.univaq.framework.exceptions.RESTWebApplicationException;
import it.univaq.framework.security.Logged;

public class RequestResource {

    private final Request request;

    RequestResource(Request request) {
        this.request = request;
        request.setRequestCharacteristics(RequestCharacteristicResourceDB.getRequestCharacteristicsByRequest(request.getKey()));
        request.setProposals(ProposalResourceDB.getProposalsByRequest(request.getKey()));
    }

    @Logged
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRequest(@Context ContainerRequestContext req) throws RESTWebApplicationException {
        int key = Integer.parseInt(req.getProperty("userid").toString());
        if(req.getSecurityContext().isUserInRole(UserRoleEnum.AMMINISTRATORE.toString()) || key == request.getOrdering().getKey() || key == request.getTechnician().getKey()) {
            return Response.ok(request).build();
        } else
            return Response.status(Response.Status.BAD_REQUEST).entity("Utente non autorizzato").build();
    }

    @Path("proposte")
    public ProposalsResource getProposalsByRequest() throws RESTWebApplicationException {
        return new ProposalsResource(request.getProposals());
    }

    @Path("caratteristiche")
    public RequestCharacteristicsResource getRequestCharacteristicsByRequest() throws RESTWebApplicationException {
        return new RequestCharacteristicsResource(request.getRequestCharacteristics());
    }
    
    @Logged
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setRequest(Request request2, @Context ContainerRequestContext req) throws RESTWebApplicationException {
        int key = Integer.parseInt(req.getProperty("userid").toString());
        if(key == request2.getOrdering().getKey() || key == request2.getTechnician().getKey()) {
            try {
                request2.setCreationDate(request.getCreationDate());
                request2.setOrdering(request.getOrdering());
                request2.setVersion(request.getVersion());
                RequestResourceDB.setRequest(request2);
                return Response.noContent().build();
            } catch (NotFoundException ex) {
                return Response.status(Response.Status.NOT_FOUND).entity("Request not found").build();
            } catch (RESTWebApplicationException ex) {
                return Response.serverError()
                        .entity(ex.getMessage()) //NEVER IN PRODUCTION!
                        .build();
            }
        } else
            return Response.status(Response.Status.BAD_REQUEST).entity("Questa richiesta non è tua, non puoi modificarla").build();
    }
}