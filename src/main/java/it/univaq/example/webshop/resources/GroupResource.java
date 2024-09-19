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
import it.univaq.example.webshop.model.Group;
import it.univaq.example.webshop.model.UserRoleEnum;
import it.univaq.framework.exceptions.RESTWebApplicationException;

@Path("database/group")
public class GroupResource {

    private static final String DS_NAME = "java:comp/env/jdbc/webshopdb";
    private static final String sGroupByID = "SELECT * FROM gruppo WHERE id=?";
    private static final String sGroupByUser = "SELECT idGruppo FROM utente_gruppo WHERE idUtente=?";
    private static final String sGroupByService = "SELECT idGruppo FROM gruppo_servizio WHERE idServizio=?";
    private static final String sGroups = "SELECT id FROM gruppo";
    private static final String sGroupByName = "SELECT id FROM gruppo where nome=?";

    private static Connection getPooledConnection() throws NamingException, SQLException {
        InitialContext ctx = new InitialContext();
        DataSource ds = (DataSource) ctx.lookup(DS_NAME);
        return ds.getConnection();
    }

    public static Group createGroup() {
        return new Group();
    }

    private static Group createGroup(ResultSet rs) throws RESTWebApplicationException {
        try {
            Group a = createGroup();
            a.setKey(rs.getInt("id"));
            a.setName(UserRoleEnum.valueOf(rs.getString("nome")));
            return a;
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("Unable to create group object form ResultSet: " + ex.getMessage());
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() throws RESTWebApplicationException {
        try {
            List<Group> l = new ArrayList<>();
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sGroups)) {
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        l.add(createGroup(rs));
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
    public Response getGroup(@PathParam("id") int group_key) throws RESTWebApplicationException {
        Group l = null;
        try {
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sGroupByID)) {
                ps.setInt(1, group_key);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        l = createGroup(rs);
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

    public static Group getGroupData(int group_key) throws RESTWebApplicationException {
        Group l = null;
        try {
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sGroupByID)) {
                ps.setInt(1, group_key);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        l = createGroup(rs);
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
    @Path("{nome: [a-zA-Z0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGroupByName(@PathParam("nome") UserRoleEnum value) throws RESTWebApplicationException {
        try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sGroupByName)) {
            ps.setString(1, value.toString());
            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return getGroup(rs.getInt("id"));
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
    @Path("{idutente: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGroupByUser(@PathParam("idutente") int user_key) throws RESTWebApplicationException {
        try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sGroupByUser)) {
            ps.setInt(1, user_key);
            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return getGroup(rs.getInt("id"));
                }
            }
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
        return null;
    }

    public static Group getGroupByUserData(int user_key) throws RESTWebApplicationException {
        try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sGroupByUser)) {
            ps.setInt(1, user_key);
            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return getGroupData(rs.getInt("id"));
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
    @Path("{idservizio: [0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGroupByService(@PathParam("idservizio") int service_key) throws RESTWebApplicationException {
        Group result = null;
        try {
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sGroupByService)) {
                ps.setInt(1, service_key);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        result = getGroupData(rs.getInt("idServizio"));
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

    public Group getGroupByServiceData(int service_key) throws RESTWebApplicationException {
        Group result = null;
        try {
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sGroupByService)) {
                ps.setInt(1, service_key);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        result = getGroupData(rs.getInt("idServizio"));
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