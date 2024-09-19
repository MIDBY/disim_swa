package it.univaq.example.webshop.resources;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

/**
 *
 * @author Didattica
 */
//questa è un'altra root resource 
//(registrata quindi anche nella RESTApp)
@Path("prodotti")
public class ProdottiResource {

    @Path("{codice: [a-z0-9]+}/fatture")
    @GET
    @Produces("application/json")
    public Response getFattureForProdotto(
            @Context UriInfo uriinfo,
            @PathParam("codice") String codice) {

        //generiamo materiale di esempio
        List<URI> l = new ArrayList<>();
        for (int i = 1; i <= 3; ++i) {
            URI uri = uriinfo.getBaseUriBuilder()
                    .path(FattureResource.class)
                    .path(FattureResource.class, "getItem")
                    .build(2020, i);
            l.add(uri);
        }

        return Response.ok(l).build();
    }
}