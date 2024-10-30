package it.univaq.example.webshop.resources;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import it.univaq.example.webshop.business.ProposalResourceDB;
import it.univaq.example.webshop.model.Proposal;
import it.univaq.example.webshop.model.ProposalStateEnum;
import it.univaq.example.webshop.model.UserRoleEnum;
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
        if(proposal2.getKey() != null && proposal2.getKey() > 0 && proposal2.getKey() == proposal.getKey()) {
            proposal.setProductName(proposal2.getProductName());
            proposal.setProducerName(proposal2.getProducerName());
            proposal.setProductDescription(proposal2.getProductDescription());
            proposal.setProductPrice(proposal2.getProductPrice());
            proposal.setUrl(proposal2.getUrl());
            proposal.setNotes(proposal2.getNotes());

            if(user_key == proposal.getTechnician().getKey()) {
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
            } else
                return Response.status(Response.Status.BAD_REQUEST).entity("Utente non autorizzato").build();
        } else
            return Response.status(Response.Status.NOT_FOUND).entity("Proposta non esistente, metodo sbagliato").build();
    }

    @Logged
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setProposalState(@QueryParam("stato_proposta") String stato_proposta, @QueryParam("motivazione") String motivazione, @Context ContainerRequestContext req) throws RESTWebApplicationException {
        if(req.getSecurityContext().isUserInRole(UserRoleEnum.ORDINANTE.toString()) || req.getSecurityContext().isUserInRole(UserRoleEnum.TECNICO.toString())) {
            if(motivazione != null)
                if(!motivazione.isEmpty())
                    proposal.setMotivation(motivazione);
            if(stato_proposta != null)
                if(ProposalStateEnum.isValidEnumValue(stato_proposta))
                    proposal.setProposalState(ProposalStateEnum.valueOf(stato_proposta));
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
        } else 
            return Response.status(Response.Status.BAD_REQUEST).entity("Utente non autorizzato").build();
    }
}