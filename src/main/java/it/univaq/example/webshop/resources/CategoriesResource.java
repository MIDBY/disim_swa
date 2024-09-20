package it.univaq.example.webshop.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import it.univaq.example.webshop.business.CategoryResourceDB;
import it.univaq.example.webshop.model.Category;
import it.univaq.framework.exceptions.RESTWebApplicationException;
import it.univaq.framework.security.Logged;

@Path("categorie")
public class CategoriesResource {

    @Logged
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCategories() throws RESTWebApplicationException {
        List<Category> l =  CategoryResourceDB.getCategoriesByDeleted(false);
        return Response.ok(l).build();
    }

    @Logged
    @GET
    @Path("genitori")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFatherCategories() throws RESTWebApplicationException {
        List<Category> result = CategoryResourceDB.getFatherCategories();
        return Response.ok(result).build();
    }

    @Logged
    @GET
    @Path("figlidi/{id: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCategoriesSonsOf(@PathParam("id") int category_key) throws RESTWebApplicationException {
        List<Category> result = CategoryResourceDB.getCategoriesSonsOf(category_key);
        return Response.ok(result).build();
    }

    @Logged
    @GET
    @Path("categoriepiuvendute")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMostSoldCategories() throws RESTWebApplicationException {
        List<Category> result = CategoryResourceDB.getMostSoldCategories();
        return Response.ok(result).build();
    }
}