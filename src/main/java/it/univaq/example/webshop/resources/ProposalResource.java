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
import it.univaq.example.webshop.business.ProposalResourceDB;
import it.univaq.example.webshop.model.Proposal;
import it.univaq.framework.exceptions.RESTWebApplicationException;
import it.univaq.framework.security.Logged;

@Path("proposta")
public class ProposalResource {

    public static Proposal createProposal() {
        return new Proposal();
    }

    @Logged
    @GET
    @Path("{id: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProposal(@PathParam("id") int proposal_key) throws RESTWebApplicationException {
        Proposal l = ProposalResourceDB.getProposal(proposal_key);
        return Response.ok(l).build();
    }   

    @Logged
    @GET
    @Path("last/{id: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLastProposalByRequest(@PathParam("id") int request_key) throws RESTWebApplicationException {
        Proposal l = ProposalResourceDB.getLastProposalByRequest(request_key);
        return Response.ok(l).build();
    }   
    
    @Logged
    @PUT
    @Consumes({"application/json"})
    public Response setProposal(Proposal proposal, @Context SecurityContext securityContext) throws RESTWebApplicationException {
        try {
            ProposalResourceDB.setProposal(proposal);
            return Response.noContent().build();
        } catch (NotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity("Proposal not found").build();
        } catch (RESTWebApplicationException ex) {
            return Response.serverError()
                    .entity(ex.getMessage()) //NEVER IN PRODUCTION!
                    .build();
        }
    }
}