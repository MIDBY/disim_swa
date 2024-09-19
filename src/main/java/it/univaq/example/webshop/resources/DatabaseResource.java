package it.univaq.example.webshop.resources;

/*
 * ATTENZIONE: il codice di questa classe dipende dalla corretta definizione delle
 * risorse presente nei file context.xml (Resource) e web.xml (resource-ref)
 * 
 * ATTENZIONE: il codice fa uso di un database configurato come segue:
 * - database 'webdb2' su DBMS MySQL in esecuzione su localhost
 * - utente 'website' con password 'webpass' autorizzato nel DBMS 
 *   a leggere i dati del suddetto database
 * - tabella 'author' presente nel suddetto database, con almeno le 
 *   colonne 'name' e 'surname' di tipo stringa

 * WARNING: this class needs the definition of an external data source to work correctly.
 * See the Resource element in context.xml and the resource-ref element in web.xml
 *
 * WARNING: the code makes use of a database configured as follows:
 * - 'webdb2' database on a MySQL DBMS running on localhost
 * - user 'website' with password 'webpass' authorized in the DBMS to read the 
 *   data of the aforementioned database
 * - 'author' table present in the aforementioned database, 
 *   with at least the string-typed 'name' and 'surname' columns
 */

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import it.univaq.framework.exceptions.RESTWebApplicationException;

/**
 *
 * @author didattica
 */
@Path("database/author")
public class DatabaseResource {

    private static final String DS_NAME = "java:comp/env/jdbc/webshopdb";
    private static final String SQL_SELECT_AUTHORS = "SELECT * FROM author";
    private static final String SQL_SELECT_AUTHOR = "SELECT * FROM author WHERE name LIKE ?";

    private static Connection getPooledConnection() throws NamingException, SQLException {
        InitialContext ctx = new InitialContext();
        DataSource ds = (DataSource) ctx.lookup(DS_NAME);
        return ds.getConnection();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() throws RESTWebApplicationException {
        try {
            List<String> l = new ArrayList<>();
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(SQL_SELECT_AUTHORS)) {
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        l.add(rs.getString("surname") + " " + rs.getString("name"));
                    }
                }
            }
            return Response.ok(l).build();
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
    }

    @GET
    @Path("{name: [a-zA-Z]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNamedItem(@PathParam("name") String name) throws RESTWebApplicationException {
        try {
            List<String> l = new ArrayList<>();
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(SQL_SELECT_AUTHOR)) {
                ps.setString(1, "%" + name + "%");
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        l.add(rs.getString("surname") + " " + rs.getString("name"));
                    }
                }
            }
            return Response.ok(l).build();
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
    }   
}