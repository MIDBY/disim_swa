package it.univaq.example.webshop.resources;

import jakarta.servlet.ServletContext;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import it.univaq.example.webshop.business.GroupResourceDB;
import it.univaq.example.webshop.business.NotificationResourceDB;
import it.univaq.example.webshop.business.RequestResourceDB;
import it.univaq.example.webshop.business.UserResourceDB;
import it.univaq.example.webshop.model.NotificationTypeEnum;
import it.univaq.example.webshop.model.Request;
import it.univaq.example.webshop.model.RequestStateEnum;
import it.univaq.example.webshop.model.User;
import it.univaq.example.webshop.model.UserRoleEnum;
import it.univaq.example.webshop.model.Utility;
import it.univaq.framework.exceptions.RESTWebApplicationException;
import it.univaq.framework.security.Logged;

@Path("utenti")
public class UsersResource {

    @Logged
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsers(@Context UriInfo uriinfo, @QueryParam("email") String email, @QueryParam("username") String username,
                            @QueryParam("accettato") String accepted, @Context ContainerRequestContext req) throws RESTWebApplicationException {
        if(req.getSecurityContext().isUserInRole(UserRoleEnum.AMMINISTRATORE.toString())) {
            List<Map<String, Object>> result = new ArrayList<>();
            List<User> l = UserResourceDB.getUsers();
            if(email != null)
                if(!email.isEmpty())
                    l.removeIf(u -> (!u.getEmail().equals(email)));
            if(username != null)
                if(!username.isEmpty())
                    l.removeIf(u -> (!u.getUsername().equals(username)));
            if(accepted != null)
                if(!accepted.isEmpty() && (accepted.equals("0") || accepted.equals("1"))) 
                    l.removeIf(u -> (u.isAccepted() != accepted.equals("1")?true:false));
                
            for(User u : l){
                UserRoleEnum ruolo = GroupResourceDB.getGroupByUser(u.getKey()).getName();
                int activeRequests = 0;
                List<Request> requests;
                if(ruolo == UserRoleEnum.ORDINANTE)    
                    requests = RequestResourceDB.getRequestsByOrdering(u.getKey());
                else
                    requests = RequestResourceDB.getRequestsByTechnician(u.getKey());

                for(Request r : requests)
                    if(r.getRequestState().equals(RequestStateEnum.NUOVO) || r.getRequestState().equals(RequestStateEnum.PRESOINCARICO) || r.getRequestState().equals(RequestStateEnum.ORDINATO))
                        activeRequests++;
                
                Map<String, Object> e = new LinkedHashMap<>();
                e.put("id", u.getKey());
                e.put("ruolo", ruolo);
                e.put("username", u.getUsername());
                e.put("email", u.getEmail());
                e.put("indirizzo", u.getAddress());
                e.put("richieste_attive", activeRequests);
                e.put("data_iscrizione", u.getSubscriptionDate().format(DateTimeFormatter.ofPattern("d/M/yyyy")));
                e.put("accettato", u.isAccepted());

                result.add(e);
            }
            if(result.size() > 0)
                return Response.ok(result).build();
            else
                return Response.status(Response.Status.NOT_FOUND).entity("Nessun utente trovato").build();
        } else 
            return Response.status(Response.Status.BAD_REQUEST).entity("Non sei l'amministratore").build();
    }

    @Logged
    @GET
    @Path("{anno: [1-9][0-9][0-9][0-9]}/{mese: [1]?[0-9]}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsersBySubscriptionMonth(@PathParam("mese") int month, @PathParam("anno") int year, @Context ContainerRequestContext req) throws RESTWebApplicationException {
        if(req.getSecurityContext().isUserInRole(UserRoleEnum.AMMINISTRATORE.toString())) {
            List<User> users = UserResourceDB.getUsersBySubscriptionMonth(month, year);
            for(User u : users){
                u.setNotifications(NotificationResourceDB.getNotificationsByUser(u.getKey()));
            }
            if(users.size() > 0)
                return Response.ok(users).build();
            else
                return Response.status(Response.Status.NOT_FOUND).entity("Nessun utente trovato").build();
        } else 
            return Response.status(Response.Status.BAD_REQUEST).entity("Non sei l'amministratore").build();
    }

    @Logged
    @GET
    @Path("gruppo/{id: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsersByGroup(@PathParam("id") int group_key, @Context ContainerRequestContext req) throws RESTWebApplicationException {
        if(req.getSecurityContext().isUserInRole(UserRoleEnum.AMMINISTRATORE.toString())) {
            List<User> result = UserResourceDB.getUsersByGroup(group_key);
            if(result.size() > 0)
                return Response.ok(result).build();
            else
                return Response.status(Response.Status.NOT_FOUND).entity("Nessun utente nel gruppo specificato trovato.").build();
        } else 
            return Response.status(Response.Status.BAD_REQUEST).entity("Non sei l'amministratore").build();
    }

    @Logged
    @GET
    @Path("gruppo/{nome: [a-zA-Z]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsersByGroup(@PathParam("nome") String group, @Context ContainerRequestContext req) throws RESTWebApplicationException {
        if(req.getSecurityContext().isUserInRole(UserRoleEnum.AMMINISTRATORE.toString())) {
            List<User> result = UserResourceDB.getUsersByGroup(UserRoleEnum.valueOf(group));
            if(result.size() > 0)
                return Response.ok(result).build();
            else
                return Response.status(Response.Status.NOT_FOUND).entity("Nessun utente nel gruppo specificato trovato.").build();
        } else 
            return Response.status(Response.Status.BAD_REQUEST).entity("Non sei l'amministratore").build();
    }

    @Path("me")
    public UserResource getMe() throws RESTWebApplicationException {
        return new UserResource();
    }   

    @Logged
    @GET
    @Path("{id: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@PathParam("id") int user_key, @Context ContainerRequestContext req) throws RESTWebApplicationException {
        if(req.getSecurityContext().isUserInRole(UserRoleEnum.AMMINISTRATORE.toString())) {
            User user = UserResourceDB.getUser(user_key);    
            if(user != null)    
                return Response.ok(user).build();
            else
                return Response.status(Response.Status.NOT_FOUND).entity("Nessun utente trovato").build();
        } else 
            return Response.status(Response.Status.BAD_REQUEST).entity("Non sei l'amministratore").build();
    }   

    @Logged
    @POST
    @Path("{id: [0-9]+}/accetta")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setUserAccepted(@PathParam("id") int user_key, @Context ContainerRequestContext req, @Context ServletContext sc) throws RESTWebApplicationException {
        if(req.getSecurityContext().isUserInRole(UserRoleEnum.AMMINISTRATORE.toString())) {
            try {
                if(user_key > 0) {
                    User u = UserResourceDB.getUser(user_key);
                    u.setAccepted(!u.isAccepted());
                    u = UserResourceDB.setUser(u);
                    String text;
                    if(u.isAccepted()) {
                        text = "accettato";
                        Utility.sendMail(sc, u.getEmail(), "Administration Mail: \nYou're been verified from our site.\nNow you can login and buy everything you want! Have a nice day");
                        Utility.sendNotification(u,"Welcome in Webshop, new client!", NotificationTypeEnum.INFO, "homepage");
                    } else {
                        text = "rifiutato";
                        Utility.sendMail(sc, u.getEmail(), "Administration Mail: \nWe're sorry, you're not longer verified on our site.\n Write on our mail to get informations about");
                        Utility.sendNotification(u,"We're sorry, you're not allowed anymore to stay in Webshop. Bye!", NotificationTypeEnum.INFO, "");
                    }
                    return Response.ok(u.getUsername() + " è stato " + text).build();
                } else
                    return Response.status(Response.Status.NOT_FOUND).entity("Utente non trovato").build();
            } catch (NotFoundException ex) {
                return Response.status(Response.Status.NOT_FOUND).entity("Utente non trovato").build();
            } catch (RESTWebApplicationException ex) {
                return Response.serverError()
                        .entity(ex.getMessage()) //NEVER IN PRODUCTION!
                        .build();
            }
        } else 
            return Response.status(Response.Status.BAD_REQUEST).entity("Non sei l'amministratore").build();
    }

    @Logged
    @POST
    @Path("{id: [0-9]+}/assumi")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeUserGroup(@PathParam("id") int user_key, @QueryParam("ruolo") String role, @Context ContainerRequestContext req, @Context ServletContext sc) throws RESTWebApplicationException {
        if(req.getSecurityContext().isUserInRole(UserRoleEnum.AMMINISTRATORE.toString())) {
            try {
                if(UserRoleEnum.valueOf(role) == UserRoleEnum.ORDINANTE){
                    UserResourceDB.changeUserGroup(user_key, UserRoleEnum.ORDINANTE);
                    User temp = UserResourceDB.getUser(user_key);
                    Utility.sendMail(sc, temp.getEmail(), "Administration Mail: \nCongratulations!\nYou're been assumed in our site as technician. From now you can work with os! Have a nice day");
                    Utility.sendNotification(temp,"Welcome in Webshop, new technician!", NotificationTypeEnum.INFO, "profile");
                }
                if(UserRoleEnum.valueOf(role) == UserRoleEnum.TECNICO){
                    UserResourceDB.changeUserGroup(user_key, UserRoleEnum.TECNICO);
                    User temp = UserResourceDB.getUser(user_key);
                    Utility.sendMail(sc, temp.getEmail(), "Administration Mail: \nYou're been fired from our site. \nHave a nice day");
                    Utility.sendNotification(temp,"We're sorry, \nyou're not allowed anymore to stay in Webshop. Bye!", NotificationTypeEnum.INFO, "");
                }
                return Response.noContent().build();

            } catch (NotFoundException ex) {
                return Response.status(Response.Status.NOT_FOUND).entity("Ruolo utente non cambiato").build();
            } catch (RESTWebApplicationException ex) {
                return Response.serverError()
                        .entity(ex.getMessage()) //NEVER IN PRODUCTION!
                        .build();
            }
        } else 
            return Response.status(Response.Status.BAD_REQUEST).entity("Non sei l'amministratore").build();
    }
}