package it.univaq.example.webshop.resources;

import jakarta.websocket.server.PathParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import it.univaq.example.webshop.business.CategoryResourceDB;
import it.univaq.example.webshop.business.CharacteristicResourceDB;
import it.univaq.example.webshop.model.Category;
import it.univaq.example.webshop.model.Characteristic;
import it.univaq.example.webshop.model.UserRoleEnum;
import it.univaq.framework.data.DataException;
import it.univaq.framework.exceptions.RESTWebApplicationException;
import it.univaq.framework.security.Logged;

public class CharacteristicsResource {

    private final Category category;

    CharacteristicsResource(Category category) {
        this.category = category;
        this.category.setCharacteristics(CharacteristicResourceDB.getCharacteristicsByCategory(category.getKey()));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCategory() throws RESTWebApplicationException {
        return Response.ok(category).build();
    }   

    @GET
    @Path("caratteristiche")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCharacteristics() throws RESTWebApplicationException {
        return Response.ok(category.getCharacteristics()).build();
    }

    @GET
    @Path("caratteristiche/albero")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCharacteristicsByCategoryTree() throws RESTWebApplicationException {
        List<Characteristic> characteristics = category.getCharacteristics();
        if(category.getFatherCategory() != null)
            tree(category.getFatherCategory(), characteristics);

        List<Map<String,Object>> result = new ArrayList<>();
        for(Characteristic c : characteristics) {
            Map<String, Object> e = new LinkedHashMap<>();
            e.put("id", c.getKey());
            e.put("nome", c.getName());
            e.put("categoria", c.getCategory().getName());
            e.put("valori_default", c.getDefaultValues());
            e.put("versione", c.getVersion());
            result.add(e);
        }
        if(result.size() > 0)
            return Response.ok(result).build();
        else
            return Response.status(Response.Status.NOT_FOUND).entity("Nessuna caratteristica trovata").build();
    }

    @GET
    @Path("{id: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCharacteristic(@PathParam("id") int characteristic_key) throws RESTWebApplicationException {
        Characteristic characteristic = getSingleCharacteristic(characteristic_key);
        if(characteristic != null)
            return Response.ok(characteristic).build();
        else
            return Response.status(Response.Status.NOT_FOUND).entity("Nessuna caratteristica trovata").build();
    }

    @Logged
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setCharacteristic(Characteristic characteristic, @Context ContainerRequestContext req) throws RESTWebApplicationException, DataException {
        if(req.getSecurityContext().isUserInRole(UserRoleEnum.AMMINISTRATORE.toString())) {
            try {
                characteristic.setCategory(category);
                for(Characteristic c : category.getCharacteristics()) {
                    if(c.getKey() == characteristic.getKey()) {  
                        if(c.getName().equalsIgnoreCase(characteristic.getName())) 
                            characteristic.setVersion(c.getVersion());
                        else {
                            if(c.getDefaultValues().equalsIgnoreCase(characteristic.getDefaultValues()))
                                characteristic.setVersion(c.getVersion());
                            else
                                characteristic.setKey(0);
                        }
                    } else {
                        if(c.getName().equalsIgnoreCase(characteristic.getName()) && characteristic.getKey() == 0) {
                            characteristic.setKey(c.getKey());
                            characteristic.setVersion(c.getVersion());
                        } 
                    }
                }
                if(!characteristic.getDefaultValues().contains("Indifferent"))
                    characteristic.setDefaultValues(characteristic.getDefaultValues() + ",Indifferent");
                CharacteristicResourceDB.setCharacteristic(characteristic);
                return Response.noContent().build();
            } catch (NotFoundException ex) {
                return Response.status(Response.Status.NOT_FOUND).entity("Characteristic not found").build();
            } catch (RESTWebApplicationException ex) {
                return Response.serverError()
                        .entity(ex.getMessage()) //NEVER IN PRODUCTION!
                        .build();
            }
        } else 
            return Response.status(Response.Status.BAD_REQUEST).entity("Non sei l'amministratore").build();
    }

    @Logged
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteCharacteristic(@QueryParam("id") int characteristic_key, @Context ContainerRequestContext req) throws RESTWebApplicationException {
        if(req.getSecurityContext().isUserInRole(UserRoleEnum.AMMINISTRATORE.toString())) {
            try {
                CharacteristicResourceDB.deleteCharacteristic(getSingleCharacteristic(characteristic_key));
                return Response.noContent().build();
            } catch (NotFoundException ex) {
                return Response.status(Response.Status.NOT_FOUND).entity("Characteristic not found").build();
            } catch (RESTWebApplicationException | DataException ex) {
                return Response.serverError()
                        .entity(ex.getMessage()) //NEVER IN PRODUCTION!
                        .build();
            }
        } else 
            return Response.status(Response.Status.BAD_REQUEST).entity("Non sei l'amministratore").build();
    }

    private void tree(Category c, List<Characteristic> chars) {
        c = CategoryResourceDB.getCategory(c.getKey());
        if(c.getFatherCategory() != null) {
            chars.addAll(CharacteristicResourceDB.getCharacteristicsByCategory(c.getKey()));
            tree(c.getFatherCategory(), chars);
        } else {
            chars.addAll(CharacteristicResourceDB.getCharacteristicsByCategory(c.getKey()));
        }
    }

    private Characteristic getSingleCharacteristic(int characteristic_key) {
        for(Characteristic c : category.getCharacteristics())
            if(c.getKey() == characteristic_key)
                return c;
        return null;
    }
}