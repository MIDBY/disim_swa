package it.univaq.framework.security;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import static jakarta.ws.rs.core.Response.Status.UNAUTHORIZED;

import it.univaq.example.webshop.business.UserResourceDB;
import it.univaq.example.webshop.model.User;
import it.univaq.example.webshop.resources.UserResource;
import jakarta.ws.rs.core.UriInfo;

@Path("auth")
public class AuthenticationRes {
    
    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response login(@Context UriInfo uriinfo,
            //un altro modo per ricevere e iniettare i parametri con JAX-RS...
            @FormParam("email") String email,
            @FormParam("password") String password) {
        try {
            
            if (AuthHelpers.getInstance().authenticateUser(email, password)) {
                String authToken = AuthHelpers.getInstance().issueToken(uriinfo, email);
                //Restituiamolo in tutte le modalità, giusto per fare un esempio...
                return Response.ok(authToken)
                        .cookie(new NewCookie.Builder("token").value(authToken).build())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken).build();
            }
        } catch (Exception e) {
            //logging dell'errore 
        }
        return Response.status(UNAUTHORIZED).build();
    }
    
    @DELETE
    @Path("logout")
    @Logged
    public Response logout(@Context ContainerRequestContext req) {
        //proprietà estratta dall'authorization header 
        //e iniettata nella request dal filtro di autenticazione
        String token = (String) req.getProperty("token");
        AuthHelpers.getInstance().revokeToken(token);
        return Response.noContent()
                //eliminaimo anche il cookie con il token
                .cookie(new NewCookie.Builder("token").value("").maxAge(0).build())
                .build();
    }

    @POST
    @Path("register")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response register(@Context UriInfo uriinfo,
            //un altro modo per ricevere e iniettare i parametri con JAX-RS...
            @FormParam("username") String username,
            @FormParam("email") String email,
            @FormParam("password") String password,
            @FormParam("address") String address,
            @FormParam("number") int number,
            @FormParam("city") String city,
            @FormParam("cap") int cap,
            @FormParam("country") String country) {
        try {
            if(UserResourceDB.getUserByEmail(email) == null) {
                User user = UserResource.createUser();
                user.setUsername(username);
                user.setEmail(email);
                user.setPassword(AuthHelpers.getPasswordHashPBKDF2(password));
                user.setAddress(address + ", " + number + ", " + city + ", " + cap + ", " + country);
                UserResourceDB.setUser(user);
                
                return Response.ok().build();
            }

        } catch (Exception e) {
            //registration error 
        }
        return Response.status(UNAUTHORIZED).build();
    }

    //Metodo per fare "refresh" del token senza ritrasmettere le credenziali
    @GET
    @Path("refresh")
    @Logged
    public Response refresh(@Context ContainerRequestContext req, @Context UriInfo uriinfo) {
        //proprietà iniettata nella request dal filtro di autenticazione
        String email = (String) req.getProperty("user");
        String newtoken = AuthHelpers.getInstance().issueToken(uriinfo, email);
        return Response.ok(newtoken)
                .cookie(new NewCookie.Builder("token").value(newtoken).build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + newtoken).build();
        
    }
}