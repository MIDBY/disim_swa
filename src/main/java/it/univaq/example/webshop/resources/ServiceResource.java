package it.univaq.example.webshop.resources;

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
import it.univaq.example.webshop.model.Service;
import it.univaq.framework.exceptions.RESTWebApplicationException;

@Path("database/service")
public class ServiceResource {

    private static final String DS_NAME = "java:comp/env/jdbc/webshopdb";
    private static final String SQL_SELECT_ALL = "SELECT * FROM utente";
    private static final String sServiceByID = "SELECT * FROM servizio WHERE id=?";
    private static final String sServiceByScript = "SELECT id FROM servizio WHERE script=?";
    private static final String sServiceByGroup = "SELECT idServizio FROM gruppo_servizio where idGruppo=?";  

    private static Connection getPooledConnection() throws NamingException, SQLException {
        InitialContext ctx = new InitialContext();
        DataSource ds = (DataSource) ctx.lookup(DS_NAME);
        return ds.getConnection();
    }

    public static Service createService() {
        return new Service();
    }

    private static Service createService(ResultSet rs) throws RESTWebApplicationException {
        try {
            Service a = createService();
            a.setKey(rs.getInt("id"));
            a.setScript(rs.getString("script"));
            return a;
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("Unable to create service object form ResultSet: " + ex.getMessage());
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() throws RESTWebApplicationException {
        try {
            List<Service> l = new ArrayList<>();
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(SQL_SELECT_ALL)) {
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        l.add(createService(rs));
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
    @Path("{id: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getService(@PathParam("id") int service_key) throws RESTWebApplicationException {
        Service l = null;
        try {
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sServiceByID)) {
                ps.setInt(1, service_key);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        l = createService(rs);
                    }
                }
            }
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
        return Response.ok(l).build();
    }   

    public static Service getServiceData(int service_key) throws RESTWebApplicationException {
        Service l = null;
        try {
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sServiceByID)) {
                ps.setInt(1, service_key);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        l = createService(rs);
                    }
                }
            }
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
        return l;
    }   
    
    @GET
    @Path("{script: [a-zA-Z0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getServiceByScript(@PathParam("script") String script) throws RESTWebApplicationException {
        try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sServiceByScript)) {
            ps.setString(1, script);
            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return getService(rs.getInt("id"));
                }
            }
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
        return null;
    }

    public static Service getServiceByScriptData(String script) throws RESTWebApplicationException {
        try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sServiceByScript)) {
            ps.setString(1, script);
            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return getServiceData(rs.getInt("id"));
                }
            }
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
        return null;
    }

    @GET
    @Path("{idgruppo: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getServicesByGroup(@PathParam("idgruppo") int group_key) throws RESTWebApplicationException {
        List<Service> result = new ArrayList<>();
        try {
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sServiceByGroup)) {
                ps.setInt(1, group_key);
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(getServiceData(rs.getInt("id")));
                    }
                }
            }
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
        return Response.ok(result).build();
    }

    public static List<Service> getServicesByGroupData(int group_key) throws RESTWebApplicationException {
        List<Service> result = new ArrayList<>();
        try {
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sServiceByGroup)) {
                ps.setInt(1, group_key);
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(getServiceData(rs.getInt("id")));
                    }
                }
            }
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
        return result;
    }
}