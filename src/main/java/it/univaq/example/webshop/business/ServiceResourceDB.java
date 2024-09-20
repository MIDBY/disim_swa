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
import it.univaq.example.webshop.model.Service;
import it.univaq.framework.exceptions.RESTWebApplicationException;

public class ServiceResourceDB {

    private static final String DS_NAME = "java:comp/env/jdbc/webshopdb";
    private static final String sServices = "SELECT * FROM servizio";
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

    public static Service getService(int service_key) throws RESTWebApplicationException {
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

    public static List<Service> getServices() throws RESTWebApplicationException {
        try {
            List<Service> l = new ArrayList<>();
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sServices)) {
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        l.add(createService(rs));
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

    public static Service getServiceByScript(String script) throws RESTWebApplicationException {
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

    public static List<Service> getServicesByGroup(int group_key) throws RESTWebApplicationException {
        List<Service> result = new ArrayList<>();
        try {
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sServiceByGroup)) {
                ps.setInt(1, group_key);
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(getService(rs.getInt("idServizio")));
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