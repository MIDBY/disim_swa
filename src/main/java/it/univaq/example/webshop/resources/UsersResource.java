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
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.univaq.example.webshop.business.NotificationResourceDB;
import it.univaq.example.webshop.business.UserResourceDB;
import it.univaq.example.webshop.model.User;
import it.univaq.example.webshop.model.UserRoleEnum;
import it.univaq.framework.data.DataException;
import it.univaq.framework.exceptions.RESTWebApplicationException;

@Path("utenti")
public class UsersResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String, Object>> getUsers(@Context UriInfo uriinfo,
                                            @QueryParam("email") String email,
                                            @QueryParam("username") String username,
                                            @QueryParam("accettato") String accepted) throws RESTWebApplicationException {
        List<Map<String, Object>> result = new ArrayList<>();
        List<User> l = UserResourceDB.getUsers();
        if(email != null && email != "")
            l.removeIf(u -> (!u.getEmail().equals(email)));
        if(username != null && username != "")
            l.removeIf(u -> (!u.getUsername().equals(username)));
        if(accepted != null && accepted != "") 
            l.removeIf(u -> (u.isAccepted() != accepted.equals("1")?true:false));
            
        for(User u : l){
            Map<String, Object> e = new HashMap<>();
            e.put("id", u.getKey());
            e.put("username", u.getUsername());
            e.put("email", u.getEmail());
            e.put("accepted", u.isAccepted());
            URI uri = uriinfo.getBaseUriBuilder()
                .path(getClass())
                .path(getClass(), "getUser")
                .build(u.getKey());
            e.put("url", uri.toString());
            result.add(e);
        }
        return result;
    }

    @GET
    @Path("{anno: [1-9][0-9][0-9][0-9]}/{mese: [1]?[0-9]}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> getUsersBySubscriptionMonth(@PathParam("mese") int month, @PathParam("anno") int year, @Context ContainerRequestContext req) throws RESTWebApplicationException {
            return UserResourceDB.getUsersBySubscriptionMonth(month, year);
            /*
            if(result.size() > 0)
                return Response.ok(result).build();
            else
                return Response.noContent().build();*/

    }
/* 
    @Logged
    @GET
    @Path("{accettato: [01]}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsersByAccepted(@PathParam("accettato") int accepted, @Context ContainerRequestContext req) throws RESTWebApplicationException {
        if(req.getSecurityContext().isUserInRole(UserRoleEnum.AMMINISTRATORE.toString())) {
            List<User> result = UserResourceDB.getUsersByAccepted(accepted==0?false:true);
            if(result.size() > 0)
                return Response.ok(result).build();
            else
                return Response.ok("Nessun utente accettato trovato.").build();
        } else
            return Response.status(Response.Status.UNAUTHORIZED).entity("La richiesta è ammessa solo dall'amministratore del sistema.").build();
    }
*/
    @GET
    @Path("gruppo/{id: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsersByGroup(@PathParam("id") int group_key, @Context ContainerRequestContext req) throws RESTWebApplicationException {
            List<User> result = UserResourceDB.getUsersByGroup(group_key);
            if(result.size() > 0)
                return Response.ok(result).build();
            else
                return Response.ok("Nessun utente nel gruppo specificato trovato.").build();
    }

    @GET
    @Path("gruppo/{nome: [a-zA-Z]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsersByGroup(@PathParam("nome") String group, @Context ContainerRequestContext req) throws RESTWebApplicationException {
            List<User> result = UserResourceDB.getUsersByGroup(UserRoleEnum.valueOf(group));
            if(result.size() > 0)
                return Response.ok(result).build();
            else
                return Response.ok("Nessun utente nel gruppo specificato trovato.").build();
    }
/*
    @GET
    @Path("{username: [a-zA-Z0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserByUsername(@PathParam("username") String username, @Context ContainerRequestContext req) throws RESTWebApplicationException {
        if(req.getSecurityContext().isUserInRole(UserRoleEnum.AMMINISTRATORE.toString())) {
            User l = UserResourceDB.getUserByUsername(username);
            return Response.ok(l).build();
        } else
            return Response.status(Response.Status.UNAUTHORIZED).entity("La richiesta è ammessa solo dall'amministratore del sistema.").build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserByEmail(@QueryParam("email") String email, @Context ContainerRequestContext req) throws RESTWebApplicationException {
        if(req.getSecurityContext().isUserInRole(UserRoleEnum.AMMINISTRATORE.toString())) {
            User l = UserResourceDB.getUserByEmail(email);
            return Response.ok(l).build();
        } else
            return Response.status(Response.Status.UNAUTHORIZED).entity("La richiesta è ammessa solo dall'amministratore del sistema.").build();
    }
 */
    @GET
    @Path("{id: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public User getUser(@PathParam("id") int user_key, @Context ContainerRequestContext req) throws RESTWebApplicationException {
            User l = UserResourceDB.getUser(user_key);
            return l;
    }   

    @GET
    @Path("notifications/{id: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserNotifications(@PathParam("id") int user_key) throws DataException{
        return Response.ok(NotificationResourceDB.getNotificationsByUser(user_key)).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setUser(User user, @Context SecurityContext securityContext) throws RESTWebApplicationException {
        try {
            if(user.getKey() == 65)
                user.setVersion(UserResourceDB.getUser(user.getKey()).getVersion());
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

    @PUT
    @Path("gruppo")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeUserGroup(@QueryParam("id") int user_key, @QueryParam("ruolo") String role, @Context ContainerRequestContext req) throws RESTWebApplicationException {
        try {
            if(UserRoleEnum.valueOf(role) == UserRoleEnum.ORDINANTE)
                UserResourceDB.changeUserGroup(user_key, UserRoleEnum.ORDINANTE);
            if(UserRoleEnum.valueOf(role) == UserRoleEnum.TECNICO)
                UserResourceDB.changeUserGroup(user_key, UserRoleEnum.TECNICO);
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