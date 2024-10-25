package it.univaq.example.webshop.resources;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
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

import java.util.List;

import it.univaq.example.webshop.business.NotificationResourceDB;
import it.univaq.example.webshop.model.Notification;
import it.univaq.example.webshop.model.NotificationTypeEnum;
import it.univaq.framework.exceptions.RESTWebApplicationException;
import it.univaq.framework.security.Logged;

public class NotificationsResource {

    private List<Notification> notifications;

    private void setNotifications(int userId) {
        this.notifications = NotificationResourceDB.getNotificationsByUser(userId);
    }

    @Logged
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNotificationsByUser(@QueryParam("letto") String letto, @QueryParam("tipo") String tipo,
                                            @Context UriInfo uriinfo, @Context ContainerRequestContext req) throws RESTWebApplicationException {
        setNotifications(Integer.parseInt(req.getProperty("userid").toString()));
        if(letto != null)
            if(!letto.isEmpty() && (letto.equals("0")) || letto.equals("1"))
                notifications.removeIf(n -> (n.isRead() != letto.equals("1")?true:false));
        if(tipo != null)
            if(!tipo.isEmpty() && NotificationTypeEnum.isValidEnumValue(tipo))
                notifications.removeIf(n -> (!n.getType().equals(NotificationTypeEnum.valueOf(tipo))));

        if(notifications.size()>0)
            return Response.ok(notifications).build();
        else
            return Response.status(Response.Status.NOT_FOUND).entity("Nessuna notifica trovata").build();
    }

    @Logged
    @GET
    @Path("{id: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNotification(@PathParam("id") int notification_key, @Context UriInfo uriinfo, @Context ContainerRequestContext req) throws RESTWebApplicationException, IllegalAccessException {
        setNotifications(Integer.parseInt(req.getProperty("userid").toString()));
        Notification notification = getSingleNotification(notification_key);
        if(notification != null)
            return Response.ok(notification).build();
        else
            return Response.status(Response.Status.NOT_FOUND).entity("Nessuna notifica trovata").build();
    }   
    
    @Logged
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setNotification(Notification notification, @Context ContainerRequestContext req) throws RESTWebApplicationException {
        setNotifications(Integer.parseInt(req.getProperty("userid").toString()));
        try {
            NotificationResourceDB.setNotification(notification);
            return Response.noContent().build();
        } catch (NotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity("Notification not found").build();
        } catch (RESTWebApplicationException ex) {
            return Response.serverError()
                    .entity(ex.getMessage()) //NEVER IN PRODUCTION!
                    .build();
        }
    }

    @Logged
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setNotificationRead(@QueryParam("id") int notification_key, @Context ContainerRequestContext req) throws RESTWebApplicationException {
        setNotifications(Integer.parseInt(req.getProperty("userid").toString()));
        try {
            Notification notification = getSingleNotification(notification_key);
            notification.setRead(!notification.isRead());
            NotificationResourceDB.setNotification(notification);
            return Response.noContent().build();
        } catch (NotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity("Notification not found").build();
        } catch (RESTWebApplicationException ex) {
            return Response.serverError()
                    .entity(ex.getMessage()) //NEVER IN PRODUCTION!
                    .build();
        }
    }

    @Logged
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteNotification(@QueryParam("id") int notification_key, @Context ContainerRequestContext req) throws RESTWebApplicationException {
        setNotifications(Integer.parseInt(req.getProperty("userid").toString()));
        try {
            NotificationResourceDB.deleteNotification(getSingleNotification(notification_key));
            return Response.noContent().build();
        } catch (NotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity("Notification not found").build();
        } catch (RESTWebApplicationException ex) {
            return Response.serverError()
                    .entity(ex.getMessage()) //NEVER IN PRODUCTION!
                    .build();
        }
    }

    @Logged
    @DELETE
    @Path("cancellatutto")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteAllNotifications(@Context ContainerRequestContext req) throws RESTWebApplicationException {
        setNotifications(Integer.parseInt(req.getProperty("userid").toString()));
        try {
            for(Notification n : notifications)
                NotificationResourceDB.deleteNotification(n);
            return Response.noContent().build();
        } catch (NotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity("Notification not found").build();
        } catch (RESTWebApplicationException ex) {
            return Response.serverError()
                    .entity(ex.getMessage()) //NEVER IN PRODUCTION!
                    .build();
        }
    }

    private Notification getSingleNotification(int notification_key) {
        for(Notification n : notifications)
            if(n.getKey() == notification_key)
                return n;
        return null;
    }
}