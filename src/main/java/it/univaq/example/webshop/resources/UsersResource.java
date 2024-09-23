package it.univaq.example.webshop.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import it.univaq.example.webshop.business.UserResourceDB;
import it.univaq.example.webshop.model.User;
import it.univaq.example.webshop.model.UserRoleEnum;
import it.univaq.framework.exceptions.RESTWebApplicationException;
import it.univaq.framework.security.Logged;

@Path("utenti")
public class UsersResource {

    @Logged
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsers(@Context ContainerRequestContext req) throws RESTWebApplicationException {
        if(req.getSecurityContext().isUserInRole(UserRoleEnum.AMMINISTRATORE.toString())) {
            List<User> l = UserResourceDB.getUsers();  
            return Response.ok(l).build();
        } else
            return Response.status(Response.Status.UNAUTHORIZED).entity("La richiesta è ammessa solo dall'amministratore del sistema.").build();
    }

    @Logged
    @GET
    @Path("{mese: [1]?[0-9]}/{anno: [1-9][0-9][0-9][0-9]}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsersBySubscriptionMonth(@PathParam("mese") int month, @PathParam("anno") int year) throws RESTWebApplicationException {
        List<User> result = UserResourceDB.getUsersBySubscriptionMonth(month, year);
        return Response.ok(result).build();
    }

    @Logged
    @GET
    @Path("{accettato: (true,false)}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsersByAccepted(@PathParam("accettato") boolean accepted) throws RESTWebApplicationException {
        List<User> result = UserResourceDB.getUsersByAccepted(accepted);
        return Response.ok(result).build();
    }

    @Logged
    @GET
    @Path("gruppo/{id: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsersByGroup(@PathParam("id") int group_key) throws RESTWebApplicationException {
        List<User> result = UserResourceDB.getUsersByGroup(group_key);
        return Response.ok(result).build();
    }

    @Logged
    @GET
    @Path("gruppo/{nome: [a-zA-Z]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsersByGroup(@PathParam("nome") String group) throws RESTWebApplicationException {
        List<User> result = UserResourceDB.getUsersByGroup(UserRoleEnum.valueOf(group));
        return Response.ok(result).build();
    }
}