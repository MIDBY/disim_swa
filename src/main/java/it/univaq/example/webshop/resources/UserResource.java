package it.univaq.example.webshop.resources;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import it.univaq.example.webshop.business.NotificationResourceDB;
import it.univaq.example.webshop.business.UserResourceDB;
import it.univaq.example.webshop.model.User;
import it.univaq.framework.exceptions.RESTWebApplicationException;
import it.univaq.framework.security.AuthHelpers;
import it.univaq.framework.security.Logged;

public class UserResource {

    private User user;

    private void setUser(int id) {
        this.user = UserResourceDB.getUser(id);
        user.setNotifications(NotificationResourceDB.getNotificationsByUser(user.getKey()));
    }

    @Logged
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@Context ContainerRequestContext req) {
        setUser(Integer.parseInt(req.getProperty("userid").toString()));
        return Response.ok(user).build();
    }

    @Path("notifiche")
    public NotificationsResource getNotificationsByUser() {
        return new NotificationsResource();
    }

    @Path("gruppo")
    public GroupResource getGroupByUser() {
        return new GroupResource();
    }

    @Logged
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setUser(User user2, @Context ContainerRequestContext req) throws RESTWebApplicationException {
        setUser(Integer.parseInt(req.getProperty("userid").toString()));
        if(user.getKey() == user2.getKey()) {
            try {     
                user2.setSubscriptionDate(user.getSubscriptionDate());
                user2.setVersion(user.getVersion());
                if(user2.getKey() == 65) {
                    user2.setAccepted(user.isAccepted());
                }
                UserResourceDB.setUser(user2);
                return Response.noContent().build();
            } catch (NotFoundException ex) {
                return Response.status(Response.Status.NOT_FOUND).entity("Utente non trovato").build();
            } catch (RESTWebApplicationException ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Errore generico").build();
            }
        } else
            return Response.status(Response.Status.BAD_REQUEST).entity("Il profilo da modificare non è il tuo").build();
    }

    @Logged
    @POST
    @Path("modifica")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response editUser(@FormParam("id") int id, @FormParam("username") String username,
                            @FormParam("email") String email, @FormParam("currentPassword") String password,
                            @FormParam("newPassword") String password2, @FormParam("indirizzo") String indirizzo,
                            @FormParam("numero") int numero, @FormParam("citta") String citta,
                            @FormParam("cap") int cap, @FormParam("nazione") String nazione, 
                            @FormParam("tipo") int tipo, @Context ContainerRequestContext req) throws RESTWebApplicationException, NoSuchAlgorithmException, InvalidKeySpecException {
            setUser(Integer.parseInt(req.getProperty("userid").toString()));
            if(user.getKey() == id) {
            try {     
                boolean modified = false;
                if(tipo == 2) {
                    if(email != null && !email.isEmpty() && !user.getEmail().equals(email)) {
                        User test = UserResourceDB.getUserByEmail(email);
                        if(test == null) {
                            user.setEmail(email);
                            modified = true;
                        } else
                            return Response.status(Response.Status.BAD_REQUEST).entity("Email già presente nel sistema").build();
                    }
                    if(password != null && !password.isEmpty() && password2 != null && !password2.isEmpty()) {
                        if(AuthHelpers.checkPasswordHashPBKDF2(password, user.getPassword())) {
                            user.setPassword(AuthHelpers.getPasswordHashPBKDF2(password));
                            modified = true;
                        } else 
                            return Response.status(Response.Status.BAD_REQUEST).entity("La password corrente non corrisponde").build();
                    }
                } else {
                    if(!username.isEmpty() && !user.getUsername().equals(username)) {
                        user.setUsername(username);
                        modified = true;
                    }
                    String address = indirizzo + ", " + numero + ", " + citta + ", " + cap + ", " + nazione;
                    if(address.length() > 8 && !user.getAddress().equals(address)) {
                        user.setAddress(address);
                        modified = true;
                    }
                }
                if(modified) {
                    UserResourceDB.setUser(user);
                    return Response.noContent().entity(tipo).build();
                } else
                    return Response.status(Response.Status.BAD_REQUEST).entity("Nessun campo aggiornato").build();
            } catch (NotFoundException ex) {
                return Response.status(Response.Status.NOT_FOUND).entity("Utente non trovato").build();
            } catch (RESTWebApplicationException ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Errore generico").build();
            }            
        } else
            return Response.status(Response.Status.BAD_REQUEST).entity("Il profilo da modificare non è il tuo").build();
    }
}