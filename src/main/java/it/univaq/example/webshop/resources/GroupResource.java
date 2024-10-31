package it.univaq.example.webshop.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import it.univaq.example.webshop.business.GroupResourceDB;
import it.univaq.example.webshop.model.Group;
import it.univaq.framework.exceptions.RESTWebApplicationException;
import it.univaq.framework.security.Logged;

public class GroupResource {

    private Group group;

    private void setGroup(int userId) {
        this.group = GroupResourceDB.getGroupByUser(userId);
    }

    @Logged
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGroup(@Context ContainerRequestContext req) throws RESTWebApplicationException {
        setGroup(Integer.parseInt(req.getProperty("userid").toString()));
        return Response.ok(group).build();
    }

    @Logged
    @GET
    @Path("nome")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGroupName(@Context ContainerRequestContext req) throws RESTWebApplicationException {
        setGroup(Integer.parseInt(req.getProperty("userid").toString()));
        return Response.ok(group.getName()).build();
    }

    @Path("servizi")
    public ServiceResource getSericesByGroup() throws RESTWebApplicationException {
        return new ServiceResource();
    }
}