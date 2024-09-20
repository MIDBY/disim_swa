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
import it.univaq.example.webshop.business.NotificationResourceDB;
import it.univaq.example.webshop.model.Notification;
import it.univaq.example.webshop.model.NotificationTypeEnum;
import it.univaq.framework.data.DataException;
import it.univaq.framework.exceptions.RESTWebApplicationException;
import it.univaq.framework.security.Logged;

@Path("notifiche")
public class NotificationsResource {

    @Logged
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNotificationsByUser(@Context ContainerRequestContext req) throws RESTWebApplicationException {
        List<Notification> l = new ArrayList<>();
        try {
            l = NotificationResourceDB.getNotificationsByUser(Integer.parseInt(req.getProperty("userid").toString()));
        } catch (NumberFormatException | DataException e) {
            e.printStackTrace();
        }  
        return Response.ok(l).build();
    }

    @Logged
    @GET
    @Path("nonletto")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNotificationsNotReadByUser(@Context ContainerRequestContext req) throws RESTWebApplicationException {
        List<Notification> result = new ArrayList<>();
        try {
            result.addAll(NotificationResourceDB.getNotificationsNotReadByUser(Integer.parseInt(req.getProperty("userid").toString())));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }  
        return Response.ok(result).build();
    }

    @Logged
    @GET
    @Path("{tipo: [a-zA-Z]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserNotificationsByType(@PathParam("tipo") String value, @Context ContainerRequestContext req) throws RESTWebApplicationException {
        List<Notification> result = new ArrayList<>();
        try {
            result.addAll(NotificationResourceDB.getUserNotificationsByType(Integer.parseInt(req.getProperty("userid").toString()), NotificationTypeEnum.valueOf(value)));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }  
        return Response.ok(result).build();
    }
}