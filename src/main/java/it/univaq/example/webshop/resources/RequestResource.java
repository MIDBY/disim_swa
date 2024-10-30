package it.univaq.example.webshop.resources;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import it.univaq.example.webshop.business.CharacteristicResourceDB;
import it.univaq.example.webshop.business.GroupResourceDB;
import it.univaq.example.webshop.business.ProposalResourceDB;
import it.univaq.example.webshop.business.RequestCharacteristicResourceDB;
import it.univaq.example.webshop.business.RequestResourceDB;
import it.univaq.example.webshop.business.UserResourceDB;
import it.univaq.example.webshop.model.OrderStateEnum;
import it.univaq.example.webshop.model.Request;
import it.univaq.example.webshop.model.RequestCharacteristic;
import it.univaq.example.webshop.model.RequestStateEnum;
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
        if(key == request.getOrdering().getKey() || key == request.getTechnician().getKey()) {
            try {
                request.setDescription(request2.getDescription());
                request.setRequestCharacteristics(request2.getRequestCharacteristics());
                for(RequestCharacteristic rc : request.getRequestCharacteristics()) {
                    RequestCharacteristic support;
                    if(rc.getKey() != null && rc.getKey() > 0)
                        support = RequestCharacteristicResourceDB.getRequestCharacteristic(rc.getKey());
                    else {
                        support = new RequestCharacteristic();
                        support.setRequest(request);
                        support.setCharacteristic(CharacteristicResourceDB.getCharacteristic(rc.getCharacteristic().getKey()));
                    }
                    support.setValue(rc.getValue());
                    RequestCharacteristicResourceDB.setRequestCharacteristic(support);
                }
                request.setNotes(request2.getNotes());
                RequestResourceDB.setRequest(request);
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

    @Logged
    @POST
    @Path("assegna")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setRequestTechnician(@Context ContainerRequestContext req) throws RESTWebApplicationException {
        int key = Integer.parseInt(req.getProperty("userid").toString());
        if(GroupResourceDB.getGroupByUser(key).getName().equals(UserRoleEnum.TECNICO)) {
            try {
                request.setTechnician(UserResourceDB.getUser(key));
                request.setRequestState(RequestStateEnum.PRESOINCARICO);
                RequestResourceDB.setRequest(request);
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

    @Logged
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setRequestOrderState(@QueryParam("stato_ordine") String order_state, @Context ContainerRequestContext req) throws RESTWebApplicationException {
        int key = Integer.parseInt(req.getProperty("userid").toString());
        if(key == request.getOrdering().getKey() || key == request.getTechnician().getKey()) {
            try {
                if(OrderStateEnum.isValidEnumValue(order_state)) {
                    request.setOrderState(OrderStateEnum.valueOf(order_state));
                    if(OrderStateEnum.valueOf(order_state) != OrderStateEnum.SPEDITO)
                        request.setRequestState(RequestStateEnum.CHIUSO);
                    RequestResourceDB.setRequest(request);
                    return Response.noContent().build();
                } else
                    return Response.status(Response.Status.NOT_FOUND).entity("Parameter not valid").build();
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

    @Logged
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cancelRequest(@Context ContainerRequestContext req) throws RESTWebApplicationException {
        int key = Integer.parseInt(req.getProperty("userid").toString());
        if(key == request.getOrdering().getKey()) {
            try {
                request.setRequestState(RequestStateEnum.ANNULLATO);
                RequestResourceDB.setRequest(request);
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