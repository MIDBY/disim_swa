package it.univaq.example.webshop.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import it.univaq.example.webshop.business.RequestCharacteristicResourceDB;
import it.univaq.example.webshop.model.RequestCharacteristic;
import it.univaq.framework.exceptions.RESTWebApplicationException;
import it.univaq.framework.security.Logged;

public class RequestCharacteristicsResource {

    @Logged
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRequestCharacteristics() throws RESTWebApplicationException {
        List<RequestCharacteristic> l =  RequestCharacteristicResourceDB.getRequestCharacteristics();
        return Response.ok(l).build();
    }

    @Logged
    @GET
    @Path("richiesta/{id: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRequestCharacteristicsByRequest(@PathParam("id") int request_key) throws RESTWebApplicationException {
        List<RequestCharacteristic> result = RequestCharacteristicResourceDB.getRequestCharacteristicsByRequest(request_key);
        return Response.ok(result).build();
    }

    @Logged
    @GET
    @Path("caratteristica/{id: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRequestCharacteristicsByCharacteristic(@PathParam("id") int characteristic_key) throws RESTWebApplicationException {
        List<RequestCharacteristic> result = RequestCharacteristicResourceDB.getRequestCharacteristicsByCharacteristic(characteristic_key);
        return Response.ok(result).build();
    }
}