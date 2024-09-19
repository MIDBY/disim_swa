package it.univaq.example.webshop.resources;

import java.io.IOException;
import java.io.OutputStream;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import it.univaq.example.webshop.model2.Fattura;
import it.univaq.framework.exceptions.RESTWebApplicationException;
/**
 *
 * @author Didattica
 */
//essendo solo una sotto-risorsa, non ha un'annotazione @Path e non
//viene registrata nella RESTApp. I path da cui potrà essere attivata
//dipendono quindi solo dalle risorse che la restituiranno 
//(in questo caso solo FatturaRespource)
public class FatturaResource {

    private final Fattura f;

    FatturaResource(Fattura f) {
        this.f=f;
    }

    @GET
    @Produces("application/json")
    public Response getItem() {
        try {
            return Response.ok(f)
                    //possiamo aggiungere alla Response vari elementi, ad esempio header...
                    .header("X-fattura-app-version", "1.0")
                    .build();
        } catch (Exception e) {
            //gestione delle eccezioni (business):
            //Modalità 1: creazione response di errore
//            return Response.serverError()
//                    .entity(e.getMessage()) //mai in produzione
//                    .build();
            //Modalità 2: incapsulamento in eccezione JAXRS compatibile
            throw new RESTWebApplicationException(e);
        }
    }

    @Path("pIVA")
    @GET
    @Produces("application/json")
    public Response getItemPIva() {
        String pIVA = f.getIntestatario().getPartitaIVA();
        return Response.ok(pIVA).build();
    }

    @PUT
    @Consumes("application/json")
    public Response updateItem(Fattura f_new) {
        //...aggiornamento della fattura f con f_new...      
        return Response.noContent().build();
    }

    @DELETE
    public Response deleteItem() {
        //...rimozione della fattura f...      
        return Response.noContent().build();
    }

    @Path("elementi")
    public ElementiResource getElementi() {
        //sotto-sotto-risorsa!
        return new ElementiResource(f);
    }

    //esempio di download binario in streaming
    @Path("attachment")
    @GET
    @Produces("application/pdf")
    public Response getAttachment() {
        final byte[] attachment = "Ciao a tutti".getBytes(); //per esempio
        StreamingOutput out = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                output.write(attachment); //esempio banale
                //ad esempio, potrei copiare su output un altro file
            }
        };
        return Response
                .ok(out)
                .header("content-disposition", "attachment; filename=fattura.pdf")
                .build();
    }
}