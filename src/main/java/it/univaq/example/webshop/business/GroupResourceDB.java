package it.univaq.example.webshop.business;

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

public class GroupResourceDB {

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

    public static Group getGroup(int group_key) throws RESTWebApplicationException {
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

    public static List<Group> getGroups() throws RESTWebApplicationException {
        try {
            List<Group> l = new ArrayList<>();
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sGroups)) {
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        l.add(createGroup(rs));
                    }
                }
            }
            return l;
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
    }

    public static Group getGroupByName(UserRoleEnum value) throws RESTWebApplicationException {
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

    public static Group getGroupByUser(int user_key) throws RESTWebApplicationException {
        try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sGroupByUser)) {
            ps.setInt(1, user_key);
            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return getGroup(rs.getInt("idGruppo"));
                }
            }
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
        return null;
    }

    public static Group getGroupByService(int service_key) throws RESTWebApplicationException {
        Group result = null;
        try {
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sGroupByService)) {
                ps.setInt(1, service_key);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        result = getGroup(rs.getInt("idGruppo"));
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