package it.univaq.example.webshop.resources;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import it.univaq.example.webshop.business.UserResourceDB;
import it.univaq.example.webshop.model.User;
import it.univaq.example.webshop.model.UserRoleEnum;
import it.univaq.framework.exceptions.RESTWebApplicationException;
import it.univaq.framework.security.Logged;

@Path("utente")
public class UserResource {

    public static User createUser() {
        return new User();
    }

    @Logged
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMe(@Context ContainerRequestContext req) throws RESTWebApplicationException {
        User l = UserResourceDB.getUser(Integer.parseInt(req.getProperty("userid").toString()));
        return Response.ok(l).build();
    } 

    @Logged
    @GET
    @Path("{id: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@PathParam("id") int user_key) throws RESTWebApplicationException {
        User l = UserResourceDB.getUser(user_key);
        return Response.ok(l).build();
    }   
    
    @Logged
    @GET
    @Path("{username: [a-zA-Z0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserByUsername(@PathParam("username") String username) throws RESTWebApplicationException {
        User l = UserResourceDB.getUserByUsername(username);
        return Response.ok(l).build();
    }

    @Logged
    @GET
    @Path("{email: [a-zA-Z0-9]+@[a-zA-Z].[a-z]}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserByEmail(@PathParam("email") String email) throws RESTWebApplicationException {
        User l = UserResourceDB.getUserByEmail(email);
        return Response.ok(l).build();
    }

    @Logged
    @PUT
    @Consumes({"application/json"})
    public Response setUser(User user, @Context SecurityContext securityContext) throws RESTWebApplicationException {
        try {
            UserResourceDB.setUser(user);
            return Response.noContent().build();
        } catch (NotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
        } catch (RESTWebApplicationException ex) {
            return Response.serverError()
                    .entity(ex.getMessage()) //NEVER IN PRODUCTION!
                    .build();
        }
    }

    @Logged
    @PUT
    @Consumes({"application/json"})
    public Response changeUserGroup(int user_key, UserRoleEnum value) throws RESTWebApplicationException {
        try {
            UserResourceDB.changeUserGroup(user_key, value);
            return Response.noContent().build();
        } catch (NotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity("User role not changed").build();
        } catch (RESTWebApplicationException ex) {
            return Response.serverError()
                    .entity(ex.getMessage()) //NEVER IN PRODUCTION!
                    .build();
        }
    }
}