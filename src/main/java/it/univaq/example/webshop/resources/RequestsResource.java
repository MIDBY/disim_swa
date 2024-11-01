package it.univaq.example.webshop.resources;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import it.univaq.example.webshop.business.CategoryResourceDB;
import it.univaq.example.webshop.business.CharacteristicResourceDB;
import it.univaq.example.webshop.business.ProposalResourceDB;
import it.univaq.example.webshop.business.RequestCharacteristicResourceDB;
import it.univaq.example.webshop.business.RequestResourceDB;
import it.univaq.example.webshop.business.UserResourceDB;
import it.univaq.example.webshop.model.OrderStateEnum;
import it.univaq.example.webshop.model.Proposal;
import it.univaq.example.webshop.model.Request;
import it.univaq.example.webshop.model.RequestCharacteristic;
import it.univaq.example.webshop.model.RequestStateEnum;
import it.univaq.example.webshop.model.UserRoleEnum;
import it.univaq.framework.exceptions.RESTWebApplicationException;
import it.univaq.framework.security.Logged;

@Path("richieste")
public class RequestsResource {

    @Logged
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRequests(@QueryParam("ordinante") int ordering_key, @QueryParam("tecnico") int technician_key,
                                @QueryParam("categoria") int category_key, @QueryParam("stato_richiesta") String requestState,
                                @QueryParam("stato_ordine") String orderState,
                                @Context UriInfo uriinfo, @Context ContainerRequestContext req) throws RESTWebApplicationException {
        List<Request> requests = new ArrayList<>();
        if(req.getSecurityContext().isUserInRole(UserRoleEnum.ORDINANTE.toString()))
            requests =  RequestResourceDB.getRequestsByOrdering(Integer.parseInt(req.getProperty("userid").toString()));
        else {
            if(req.getSecurityContext().isUserInRole(UserRoleEnum.TECNICO.toString()))
                requests =  RequestResourceDB.getRequestsByTechnician(Integer.parseInt(req.getProperty("userid").toString()));
            else 
                requests =  RequestResourceDB.getRequests();
        }
        if(ordering_key > 0)
            requests.removeIf(r -> (r.getOrdering().getKey() != ordering_key));
        if(technician_key > 0)
            requests.removeIf(r -> (r.getTechnician().getKey() != technician_key));
        if(category_key > 0)
            requests.removeIf(r -> (r.getCategory().getKey() != category_key));
        if(requestState != null)
            if(!requestState.isEmpty() && RequestStateEnum.isValidEnumValue(requestState))
                requests.removeIf(r -> (!r.getRequestState().equals(RequestStateEnum.valueOf(requestState))));
        if(orderState != null)
            if(!orderState.isEmpty() && OrderStateEnum.isValidEnumValue(orderState))
                requests.removeIf(r -> (!r.getOrderState().equals(OrderStateEnum.valueOf(orderState)))); 
        List<Map<String, Object>> result = new ArrayList<>();
        for(Request r : requests) {
            Map<String, Object> e = new LinkedHashMap<>();
            e.put("id", r.getKey());
            e.put("categoria", r.getCategory());
            e.put("titolo", r.getTitle());
            e.put("descrizione", r.getDescription());
            e.put("ordinante", r.getOrdering().getUsername());
            if(r.getTechnician() != null)
                e.put("tecnico", r.getTechnician().getUsername());
            e.put("data_creazione", r.getCreationDate().format(DateTimeFormatter.ofPattern("d/M/yyyy")));
            e.put("stato_richiesta", r.getRequestState());
            e.put("stato_ordine", r.getOrderState().toString());
            e.put("caratteristiche", RequestCharacteristicResourceDB.getRequestCharacteristicsByRequest(r.getKey()));
            Proposal proposal = ProposalResourceDB.getLastProposalByRequest(r.getKey());
            if(proposal != null)
                e.put("proposta", proposal);
            e.put("proposte", ProposalResourceDB.getProposalsByRequest(r.getKey()));
            result.add(e);
        }
        if(result.size() > 0)
            return Response.ok(result).build();
        else
            return Response.status(Response.Status.NOT_FOUND).entity("Nessuna richiesta trovata").build();
    }

    @Logged
    @GET
    @Path("{anno: [1-9][0-9][0-9][0-9]}/{mese: [1]?[0-9]}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRequestsByCreationMonth(@PathParam("mese") int month, @PathParam("anno") int year, @Context ContainerRequestContext req) throws RESTWebApplicationException {
        if(req.getSecurityContext().isUserInRole(UserRoleEnum.AMMINISTRATORE.toString())) {
            List<Request> result = RequestResourceDB.getRequestsByCreationMonth(month, year);
            if(result.size() > 0)
                return Response.ok(result).build();
            else
                return Response.status(Response.Status.NOT_FOUND).entity("Nessuna richiesta trovata").build();
        } else
            return Response.status(Response.Status.BAD_REQUEST).entity("Non sei l'amministratore, non puoi accedere a queste informazioni").build();
    }

    @Logged
    @GET
    @Path("nonassegnate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUnassignedRequests(@Context ContainerRequestContext req) throws RESTWebApplicationException {
        if(req.getSecurityContext().isUserInRole(UserRoleEnum.TECNICO.toString())) {
            List<Request> result = RequestResourceDB.getUnassignedRequests();
            for(Request r : result)
                r.setRequestCharacteristics(RequestCharacteristicResourceDB.getRequestCharacteristicsByRequest(r.getKey()));
            if(result.size() > 0)
                return Response.ok(result).build();
            else
                return Response.status(Response.Status.NOT_FOUND).entity("Nessuna richiesta trovata").build();
        } else 
            return Response.status(Response.Status.BAD_REQUEST).entity("Non sei un tecnico in cerca di lavoro").build();
    }

    @Path("{id: [0-9]+}")
    public RequestResource getRequest(@PathParam("id") int request_key) throws RESTWebApplicationException {
        return new RequestResource(RequestResourceDB.getRequest(request_key));
    }   
    
    @Logged
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addRequest(Request request, @Context ContainerRequestContext req) throws RESTWebApplicationException {
        if(req.getSecurityContext().isUserInRole(UserRoleEnum.ORDINANTE.toString())) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                String title = LocalDateTime.now().format(formatter);
                request.setTitle("Request#" + title);
                request.setCreationDate(LocalDate.now());
                request.setCategory(CategoryResourceDB.getCategory(request.getCategory().getKey()));
                request.setOrdering(UserResourceDB.getUser(Integer.parseInt(req.getProperty("userid").toString())));
                request = RequestResourceDB.setRequest(request);

                if(request.getRequestCharacteristics() != null) {
                    for (RequestCharacteristic rc : request.getRequestCharacteristics()) {
                        rc.setCharacteristic(CharacteristicResourceDB.getCharacteristic(rc.getCharacteristic().getKey()));
                        rc.setRequest(request);
                        RequestCharacteristicResourceDB.setRequestCharacteristic(rc);
                    }
                }
                return Response.noContent().build();
            } catch (NotFoundException ex) {
                return Response.status(Response.Status.NOT_FOUND).entity("Richiesta non trovata").build();
            } catch (RESTWebApplicationException ex) {
                return Response.serverError()
                        .entity(ex.getMessage()) //NEVER IN PRODUCTION!
                        .build();
            }
        } else 
            return Response.status(Response.Status.BAD_REQUEST).entity("Non sei un cliente, non puoi creare richieste").build();
    }
}