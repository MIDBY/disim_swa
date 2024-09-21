package it.univaq.example.webshop.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import it.univaq.example.webshop.business.ProposalResourceDB;
import it.univaq.example.webshop.model.Proposal;
import it.univaq.example.webshop.model.ProposalStateEnum;
import it.univaq.framework.exceptions.RESTWebApplicationException;
import it.univaq.framework.security.Logged;

@Path("proposte")
public class ProposalsResource {

    @Logged
    @GET
    @Path("{idrichiesta: [0-9]+")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProposalsByRequest(@PathParam("idrichiesta") int request_key) throws RESTWebApplicationException {
        List<Proposal> result = ProposalResourceDB.getProposalsByRequest(request_key);
        return Response.ok(result).build();
    }

    @Logged
    @GET
    @Path("{idtecnico: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProposalsByTechnician(@PathParam("idtecnico") int user_key) throws RESTWebApplicationException {
        List<Proposal> result = ProposalResourceDB.getProposalsByTechnician(user_key);
        return Response.ok(result).build();
    }

    @Logged
    @GET
    @Path("{statoproposta: [a-zA-Z]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProposalsByState(@PathParam("statoproposta") String value) throws RESTWebApplicationException {
        List<Proposal> result = ProposalResourceDB.getProposalsByState(ProposalStateEnum.valueOf(value));
        return Response.ok(result).build();
    }

    @Logged
    @GET
    @Path("{mese: [1]?[0-9]}/{anno: [1-9][0-9][0-9][0-9]}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProposalsByCreationMonth(@PathParam("mese") int month, @PathParam("anno") int year) throws RESTWebApplicationException {
        List<Proposal> result = ProposalResourceDB.getProposalsByCreationMonth(month, year);
        return Response.ok(result).build();
    }
}