package it.univaq.example.webshop.resources;

import jakarta.servlet.ServletContext;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
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

import java.time.LocalDateTime;
import java.util.List;

import it.univaq.example.webshop.business.ProposalResourceDB;
import it.univaq.example.webshop.business.RequestResourceDB;
import it.univaq.example.webshop.business.UserResourceDB;
import it.univaq.example.webshop.model.NotificationTypeEnum;
import it.univaq.example.webshop.model.Proposal;
import it.univaq.example.webshop.model.ProposalStateEnum;
import it.univaq.example.webshop.model.UserRoleEnum;
import it.univaq.example.webshop.model.Utility;
import it.univaq.framework.exceptions.RESTWebApplicationException;
import it.univaq.framework.security.Logged;

public class ProposalsResource {

    private final List<Proposal> proposals;

    ProposalsResource(List<Proposal> proposals) {
        this.proposals = proposals;
    }

    @Logged
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProposalsByRequest(@QueryParam("tecnico") int technician_key, @QueryParam("stato") String proposalState,
                                        @Context UriInfo uriinfo) throws RESTWebApplicationException {
        if(technician_key > 0)
            proposals.removeIf(p -> (p.getTechnician().getKey() != technician_key));
        if(!proposalState.isEmpty() && ProposalStateEnum.isValidEnumValue(proposalState))
            proposals.removeIf(p -> (!p.getProposalState().equals(ProposalStateEnum.valueOf(proposalState))));
        if(proposals.size() > 0)
            return Response.ok(proposals).build();
        else
            return Response.status(Response.Status.NOT_FOUND).entity("Nessuna proposta trovata").build();
    }

    @Logged
    @GET
    @Path("{anno: [1-9][0-9][0-9][0-9]}/{mese: [1]?[0-9]}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProposalsByCreationMonth(@PathParam("mese") int month, @PathParam("anno") int year) throws RESTWebApplicationException {
        for(Proposal p : proposals)
            if(!(p.getCreationDate().getYear() >= year && p.getCreationDate().getMonthValue() >= month))
                proposals.remove(p);
        if(proposals.size() > 0)
            return Response.ok(proposals).build();
        else
            return Response.status(Response.Status.NOT_FOUND).entity("Nessuna proposta trovata").build();
    }

    @Path("{id: [0-9]+}")
    public ProposalResource getProposal(@PathParam("id") int proposal_key) throws RESTWebApplicationException {
        return new ProposalResource(getSingleProposal(proposal_key));
    }   

    @Logged
    @GET
    @Path("ultima")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLastProposalOfRequest() throws RESTWebApplicationException {
        Proposal p = getLastProposal();
        if(p != null)
            return Response.ok(p).build();
        else
            return Response.status(Response.Status.NOT_FOUND).entity("Nessuna proposta trovata").build();
    }   
    
    @Logged
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addProposal(Proposal proposal2, @Context ContainerRequestContext req, @Context ServletContext sc) throws RESTWebApplicationException {
        int user_key = Integer.parseInt(req.getProperty("userid").toString());
        if(req.getSecurityContext().isUserInRole(UserRoleEnum.TECNICO.toString())) {
            if(proposal2.getKey() == null || proposal2.getKey() == 0) {
                if(proposals.size() > 0) {
                    Proposal support = getLastProposal();
                    proposal2.setTechnician(support.getTechnician());
                    proposal2.setRequest(support.getRequest());
                } else {
                    proposal2.setTechnician(UserResourceDB.getUser(user_key));
                    proposal2.setRequest(RequestResourceDB.getRequest(proposal2.getRequest().getKey()));
                }
                proposal2.setCreationDate(LocalDateTime.now());
                proposal2.setProposalState(ProposalStateEnum.INATTESA);
                proposal2.setVersion(0);
                
                if(user_key == proposal2.getTechnician().getKey()) {
                    try {
                        ProposalResourceDB.setProposal(proposal2);
                        Utility.sendMail(sc,proposal2.getRequest().getOrdering().getEmail(), "Info mail: \nYour request: "+proposal2.getRequest().getTitle()+" has received a new proposal, go to check it!");
                        Utility.sendNotification(proposal2.getRequest().getOrdering(), "Request: "+proposal2.getRequest().getTitle()+".\n Our technician has sent a new proposal to you, go to check it!", NotificationTypeEnum.NUOVO, "requests"); 
                        return Response.noContent().build();
                    } catch (RESTWebApplicationException ex) {
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Errore generico").build();
                    }
                } else
                    return Response.status(Response.Status.BAD_REQUEST).entity("Tecnico non autorizzato").build();
            } else
                return Response.status(Response.Status.CONFLICT).entity("Proposta già esistente, metodo sbagliato").build();
        } else
            return Response.status(Response.Status.NOT_FOUND).entity("Utente non autorizzato").build();
    }

    private Proposal getSingleProposal(int proposal_key) {
        if(proposal_key > 0)
            for(Proposal p : proposals)
                if(p.getKey() == proposal_key)
                    return p;
        return null;
    }

    private Proposal getLastProposal() {
        Proposal result = null;
        for(Proposal p : proposals)
            if(result == null || p.getKey() > result.getKey()) {
                result = p;
            }
        return result;
    }
}