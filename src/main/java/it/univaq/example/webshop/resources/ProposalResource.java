package it.univaq.example.webshop.resources;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import it.univaq.example.webshop.business.ProposalResourceDB;
import it.univaq.example.webshop.model.Proposal;
import it.univaq.framework.exceptions.RESTWebApplicationException;
import it.univaq.framework.security.Logged;

public class ProposalResource {

    private final Proposal proposal;

    ProposalResource(Proposal proposal) {
        this.proposal = proposal;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProposal() throws RESTWebApplicationException {
        if(proposal != null)
            return Response.ok(proposal).build();
        else
            return Response.status(Response.Status.NOT_FOUND).entity("Nessuna proposta trovata").build();
    }   
    
    @Logged
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setProposal(Proposal proposal2, @Context ContainerRequestContext req) throws RESTWebApplicationException {
        int user_key = Integer.parseInt(req.getProperty("userid").toString());
        if(proposal2.getKey() > 0 && proposal2.getKey() == proposal.getKey()) {
            proposal2.setTechnician(proposal.getTechnician());
            proposal2.setRequest(proposal.getRequest());
            proposal2.setCreationDate(proposal.getCreationDate());

            if(user_key == proposal2.getTechnician().getKey() || user_key == proposal2.getRequest().getOrdering().getKey()) {
                try {
                    ProposalResourceDB.setProposal(proposal2);
                    return Response.noContent().build();
                } catch (NotFoundException ex) {
                    return Response.status(Response.Status.NOT_FOUND).entity("Proposal not found").build();
                } catch (RESTWebApplicationException ex) {
                    return Response.serverError()
                            .entity(ex.getMessage()) //NEVER IN PRODUCTION!
                            .build();
                }
            } else
                return Response.status(Response.Status.BAD_REQUEST).entity("Utente non autorizzato").build();
        } else
            return Response.status(Response.Status.NOT_FOUND).entity("Proposta non esistente, metodo sbagliato").build();
    }
}