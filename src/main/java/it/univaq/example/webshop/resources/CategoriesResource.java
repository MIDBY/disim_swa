package it.univaq.example.webshop.resources;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
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
import jakarta.ws.rs.core.UriInfo;

import java.util.Iterator;
import java.util.List;

import it.univaq.example.webshop.business.CategoryResourceDB;
import it.univaq.example.webshop.business.CharacteristicResourceDB;
import it.univaq.example.webshop.business.ImageResourceDB;
import it.univaq.example.webshop.business.RequestCharacteristicResourceDB;
import it.univaq.example.webshop.model.Category;
import it.univaq.example.webshop.model.Characteristic;
import it.univaq.example.webshop.model.RequestCharacteristic;
import it.univaq.example.webshop.model.UserRoleEnum;
import it.univaq.framework.data.DataException;
import it.univaq.framework.exceptions.RESTWebApplicationException;
import it.univaq.framework.security.Logged;

@Path("categorie")
public class CategoriesResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCategories(@Context UriInfo uriinfo) throws RESTWebApplicationException {
        List<Category> categories =  CategoryResourceDB.getCategoriesByDeleted(false);
        for(Category c : categories) {
            c.setCharacteristics(CharacteristicResourceDB.getCharacteristicsByCategory(c.getKey()));
        }
        if(categories.size() > 0)
            return Response.ok(categories).build();
        else
            return Response.status(Response.Status.NOT_FOUND).entity("Nessuna categoria trovata").build();
    }

    @GET
    @Path("genitori")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFatherCategories() throws RESTWebApplicationException {
        List<Category> result = CategoryResourceDB.getFatherCategories();
        if(result.size() > 0)
            return Response.ok(result).build();
        else
            return Response.status(Response.Status.NOT_FOUND).entity("Nessuna categoria trovata").build();
    }

    @GET
    @Path("piuvendute")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMostSoldCategories() throws RESTWebApplicationException {
        List<Category> result = CategoryResourceDB.getMostSoldCategories();
        if(result.size() > 0)
            return Response.ok(result).build();
        else
            return Response.status(Response.Status.NOT_FOUND).entity("Nessuna categoria trovata").build();
    }

    @Path("{id: [0-9]+}")
    public CharacteristicsResource getCategory(@PathParam("id") int category_key) throws RESTWebApplicationException {
        Category category = CategoryResourceDB.getCategory(category_key);
        return new CharacteristicsResource(category);
    }   
    
    @Logged
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setCategory(Category category, @Context ContainerRequestContext req) throws RESTWebApplicationException, DataException {
        if(req.getSecurityContext().isUserInRole(UserRoleEnum.AMMINISTRATORE.toString())) {
            try {
                int image_to_delete = 0;
                //formatto i valori di default come "valore1","valore2"... così via senza spazi tra le virgole
                for(Characteristic s : category.getCharacteristics()) {
                    String value = "";
                    String[] elems = s.getDefaultValues().split(",");
                    for(String elem : elems) {
                        value += elem.trim() + ",";
                    }
                    if(value.contains("Indifferent")){
                        value = value.replaceAll("Indifferent,", "");
                        value = value.replaceAll("Indifferent", "");
                    }
                    value += "Indifferent";
                    s.setDefaultValues(value);
                }

                if(category.getKey() > 0) { 
                    Category cat = CategoryResourceDB.getCategory(category.getKey());
                    cat.setCharacteristics(CharacteristicResourceDB.getCharacteristicsByCategory(cat.getKey()));
                    category.setVersion(cat.getVersion());

                    if(cat.getImage().getKey() != category.getImage().getKey()) 
                        image_to_delete = cat.getImage().getKey();

                    List<Characteristic> oldChars = cat.getCharacteristics();
                    List<RequestCharacteristic> usedChars = RequestCharacteristicResourceDB.getRequestCharacteristics();

                    for(Characteristic n : category.getCharacteristics()) {
                        Iterator<Characteristic> iterator = oldChars.iterator();
                        while (iterator.hasNext()) {
                            Characteristic o = iterator.next();
                            if(n.getKey() == o.getKey()) {
                                if(n.getName().equalsIgnoreCase(o.getName())) {
                                    //stesso nome e stessa chiave, vecchia char riusata
                                    n.setVersion(o.getVersion());
                                    iterator.remove();
                                } else {
                                    //stessa chiave ma nome diverso. E' stata usata? Si: elimino la caratteristica dalla tabella richiesta_caratteristica (elimino la vecchia caratteristica lasciandola li)
                                    for(RequestCharacteristic u : usedChars)
                                        if(n.getKey() == u.getCharacteristic().getKey()) 
                                            RequestCharacteristicResourceDB.deleteRequestCharacteristic(u);
                                    n.setKey(0);
                                }
                            } else {
                                if(n.getKey() == 0) {
                                    //può essere una esistente con stesso nome
                                    if(n.getName().equalsIgnoreCase(o.getName())) {
                                        n.setKey(o.getKey());
                                        n.setVersion(o.getVersion());
                                        iterator.remove();
                                    }
                                }
                            }
                        }
                    }

                    for(Characteristic c : oldChars) {
                        CharacteristicResourceDB.deleteCharacteristic(c);
                    }
                }
            
                if(category.getFatherCategory() != null && (category.getFatherCategory().getKey() == null || category.getFatherCategory().getKey() == 0))
                        category.setFatherCategoryNull();

                category = CategoryResourceDB.setCategory(category);
                for(Characteristic c : category.getCharacteristics()) {
                    c.setCategory(category);
                    CharacteristicResourceDB.setCharacteristic(c);
                }
                if(image_to_delete > 0)
                    ImageResourceDB.deleteImage(image_to_delete);
                return Response.noContent().build();
            } catch (NotFoundException ex) {
                return Response.status(Response.Status.NOT_FOUND).entity("Categoria non trovata").build();
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
    public Response deleteCategory(@QueryParam("id") int category_key, @Context ContainerRequestContext req) throws RESTWebApplicationException {
        if(req.getSecurityContext().isUserInRole(UserRoleEnum.AMMINISTRATORE.toString())) {
            try {
                CategoryResourceDB.deleteCategory(category_key);
                return Response.noContent().build();
            } catch (NotFoundException ex) {
                return Response.status(Response.Status.NOT_FOUND).entity("Categoria non trovata").build();
            } catch (RESTWebApplicationException | DataException ex) {
                return Response.serverError()
                        .entity(ex.getMessage()) //NEVER IN PRODUCTION!
                        .build();
            }
        } else 
            return Response.status(Response.Status.BAD_REQUEST).entity("Non sei l'amministratore").build();
    }
}