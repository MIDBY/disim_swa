package it.univaq.example.webshop.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

import it.univaq.example.webshop.business.GroupResourceDB;
import it.univaq.example.webshop.model.Group;
import it.univaq.example.webshop.model.UserRoleEnum;
import it.univaq.framework.exceptions.RESTWebApplicationException;
import it.univaq.framework.security.Logged;

@Path("gruppo")
public class GroupResource {

    @Logged
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGroups() throws RESTWebApplicationException {
        List<Group> l = GroupResourceDB.getGroups();
        return Response.ok(l).build();
    }

    @Logged
    @GET
    @Path("{id: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGroup(@PathParam("id") int group_key) throws RESTWebApplicationException {
        Group l = GroupResourceDB.getGroup(group_key);
        return Response.ok(l).build();
    }   
    
    @Logged
    @GET
    @Path("{nome: [a-zA-Z0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGroupByName(@PathParam("nome") UserRoleEnum value) throws RESTWebApplicationException {
        Group l = GroupResourceDB.getGroupByName(value);
        return Response.ok(l).build();
    }

    @Logged
    @GET
    @Path("{idutente: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGroupByUser(@PathParam("idutente") int user_key) throws RESTWebApplicationException {
        Group l = GroupResourceDB.getGroupByUser(user_key);
        return Response.ok(l).build();
    }

    @Logged
    @GET
    @Path("{idservizio: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGroupByService(@PathParam("idservizio") int service_key) throws RESTWebApplicationException {
        Group result = GroupResourceDB.getGroupByService(service_key);
        return Response.ok(result).build();
    }
}