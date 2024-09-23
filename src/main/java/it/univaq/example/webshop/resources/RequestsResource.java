package it.univaq.example.webshop.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import it.univaq.example.webshop.business.RequestResourceDB;
import it.univaq.example.webshop.model.OrderStateEnum;
import it.univaq.example.webshop.model.Request;
import it.univaq.example.webshop.model.RequestStateEnum;
import it.univaq.example.webshop.model.UserRoleEnum;
import it.univaq.framework.exceptions.RESTWebApplicationException;
import it.univaq.framework.security.Logged;

@Path("richieste")
public class RequestsResource {


    @Logged
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRequests(@Context ContainerRequestContext req) throws RESTWebApplicationException {
        List<Request> l = new ArrayList<>();
        if(req.getSecurityContext().isUserInRole(UserRoleEnum.ORDINANTE.toString()))
            l =  RequestResourceDB.getRequestsByOrdering(Integer.parseInt(req.getProperty("userid").toString()));
        else {
            if(req.getSecurityContext().isUserInRole(UserRoleEnum.TECNICO.toString()))
                l =  RequestResourceDB.getRequestsByTechnician(Integer.parseInt(req.getProperty("userid").toString()));
            else 
                l =  RequestResourceDB.getRequests();
        }
        return Response.ok(l).build();
    }

    @Logged
    @GET
    @Path("tutte")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRequests() throws RESTWebApplicationException {
        List<Request> l =  RequestResourceDB.getRequests();
        return Response.ok(l).build();
    }

    @Logged
    @GET
    @Path("categoria/{id: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRequestsByCategory(@PathParam("id") int category_key) throws RESTWebApplicationException {
        List<Request> result = RequestResourceDB.getRequestsByCategory(category_key);
        return Response.ok(result).build();
    }

    @Logged
    @GET
    @Path("ordinante/{id: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRequestsByOrdering(@PathParam("id") int user_key) throws RESTWebApplicationException {
        List<Request> result = RequestResourceDB.getRequestsByOrdering(user_key);
        return Response.ok(result).build();
    }

    @Logged
    @GET
    @Path("tecnico/{id: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRequestsByTechnician(@PathParam("id") int user_key) throws RESTWebApplicationException {
        List<Request> result = RequestResourceDB.getRequestsByTechnician(user_key);
        return Response.ok(result).build();
    }

    @Logged
    @GET
    @Path("{stato: [a-zA-Z]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRequestsByRequestState(@PathParam("stato") String value) throws RESTWebApplicationException {
        List<Request> result = RequestResourceDB.getRequestsByRequestState(RequestStateEnum.valueOf(value));
        return Response.ok(result).build();
    }

    @Logged
    @GET
    @Path("ordine/{stato: [a-zA-Z]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRequestsByOrderState(@PathParam("stato") String value) throws RESTWebApplicationException {
        List<Request> result = RequestResourceDB.getRequestsByOrderState(OrderStateEnum.valueOf(value));
        return Response.ok(result).build();
    }

    @Logged
    @GET
    @Path("{mese: [1]?[0-9]}/{anno: [1-9][0-9][0-9][0-9]}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRequestsByCreationMonth(@PathParam("mese") int month, @PathParam("anno") int year) throws RESTWebApplicationException {
        List<Request> result = RequestResourceDB.getRequestsByCreationMonth(month, year);
        return Response.ok(result).build();
    }

    @Logged
    @GET
    @Path("nonassegnate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUnassignedRequests() throws RESTWebApplicationException {
        List<Request> result = RequestResourceDB.getUnassignedRequests();
        return Response.ok(result).build();
    }
}