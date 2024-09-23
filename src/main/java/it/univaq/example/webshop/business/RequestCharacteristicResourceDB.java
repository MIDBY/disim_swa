package it.univaq.example.webshop.business;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import it.univaq.example.webshop.model.RequestCharacteristic;
import it.univaq.framework.data.DataException;
import it.univaq.framework.data.OptimisticLockException;
import it.univaq.framework.exceptions.RESTWebApplicationException;

public class RequestCharacteristicResourceDB {

    private static final String DS_NAME = "java:comp/env/jdbc/webshopdb";
    private static final String sRequestCharacteristicByID = "SELECT * FROM richiesta_caratteristica WHERE id=?";
    private static final String sRequestCharacteristicsByRequest = "SELECT id FROM richiesta_caratteristica where idRichiesta=?";
    private static final String sRequestCharacteristicsByCharacteristic = "SELECT id FROM richiesta_caratteristica WHERE idCaratteristica=?";
    private static final String sRequestCharacteristics = "SELECT id from richiesta_caratteristica";
    private static final String iRequestCharacteristic = "INSERT INTO richiesta_caratteristica (idRichiesta,idCaratteristica,valore) VALUES (?,?,?)";
    private static final String uRequestCharacteristic = "UPDATE richiesta_caratteristica SET idRichiesta=?,idCaratteristica=?,valore=?,versione=? WHERE id=? and versione=?";
    private static final String dRequestCharacteristic = "DELETE FROM richiesta_caratteristica WHERE id=?";

    private static Connection getPooledConnection() throws NamingException, SQLException {
        InitialContext ctx = new InitialContext();
        DataSource ds = (DataSource) ctx.lookup(DS_NAME);
        return ds.getConnection();
    }


    public static RequestCharacteristic createRequestCharacteristic() {
        return new RequestCharacteristic();
    }

    private static RequestCharacteristic createRequestCharacteristic(ResultSet rs) throws RESTWebApplicationException {
        try {
            RequestCharacteristic a = createRequestCharacteristic();
            a.setKey(rs.getInt("id"));
            a.setRequest(RequestResourceDB.getRequest(rs.getInt("idRichiesta")));
            a.setCharacteristic(CharacteristicResourceDB.getCharacteristic(rs.getInt("idCaratteristica")));
            a.setValue(rs.getString("valore"));
            a.setVersion(rs.getLong("versione"));
            return a;
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("Unable to create request characteristic object form ResultSet: " + ex.getMessage());
        }
    }

    public static RequestCharacteristic getRequestCharacteristic(int requestCharacteristic_key) throws RESTWebApplicationException {
        RequestCharacteristic l = null;
        try {
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sRequestCharacteristicByID)) {
                ps.setInt(1, requestCharacteristic_key);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        l = createRequestCharacteristic(rs);
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

    
    public static List<RequestCharacteristic> getRequestCharacteristicsByRequest(int request_key) throws RESTWebApplicationException {
        try {
            List<RequestCharacteristic> l = new ArrayList<>();
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sRequestCharacteristicsByRequest)) {
                ps.setInt(1, request_key);
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        l.add(getRequestCharacteristic(rs.getInt("id")));
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

    public static List<RequestCharacteristic> getRequestCharacteristicsByCharacteristic(int characteristic_key) throws RESTWebApplicationException {
        try {
            List<RequestCharacteristic> l = new ArrayList<>();
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sRequestCharacteristicsByCharacteristic)) {
                ps.setInt(1, characteristic_key);
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        l.add(getRequestCharacteristic(rs.getInt("id")));
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
        
    public static List<RequestCharacteristic> getRequestCharacteristics() throws RESTWebApplicationException {
        try {
            List<RequestCharacteristic> l = new ArrayList<>();
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sRequestCharacteristics)) {
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        l.add(getRequestCharacteristic(rs.getInt("id")));
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

    public static RequestCharacteristic setRequestCharacteristic(RequestCharacteristic requestCharacteristic) throws RESTWebApplicationException {
        try {
            if (requestCharacteristic.getKey() != null && requestCharacteristic.getKey() > 0) { //update
                try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(uRequestCharacteristic)) {
                    if (requestCharacteristic.getRequest() != null) {
                        ps.setInt(1, requestCharacteristic.getRequest().getKey());
                    } else {
                        ps.setNull(1, java.sql.Types.INTEGER);
                    }  
                    if (requestCharacteristic.getCharacteristic() != null) {
                        ps.setInt(2, requestCharacteristic.getCharacteristic().getKey());
                    } else {
                        ps.setNull(2, java.sql.Types.INTEGER);
                    }  
                    ps.setString(3, requestCharacteristic.getValue());

                    long current_version = requestCharacteristic.getVersion();
                    long next_version = current_version + 1;

                    ps.setLong(4, next_version);
                    ps.setInt(5, requestCharacteristic.getKey());
                    ps.setLong(6, current_version);

                    if (ps.executeUpdate() == 0) {
                        throw new RESTWebApplicationException("RequestCharacteristic not updated");
                    } else {
                        requestCharacteristic.setVersion(next_version);
                    }
                }
            } else { //insert
                try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(iRequestCharacteristic, Statement.RETURN_GENERATED_KEYS)) {
                    if (requestCharacteristic.getRequest() != null) {
                        ps.setInt(1, requestCharacteristic.getRequest().getKey());
                    } else {
                        ps.setNull(1, java.sql.Types.INTEGER);
                    }  
                    if (requestCharacteristic.getCharacteristic() != null) {
                        ps.setInt(2, requestCharacteristic.getCharacteristic().getKey());
                    } else {
                        ps.setNull(2, java.sql.Types.INTEGER);
                    }  
                    ps.setString(3, requestCharacteristic.getValue());

                    if (ps.executeUpdate() == 1) {
                        try ( ResultSet keys = ps.getGeneratedKeys()) {
                            if (keys.next()) {
                                int key = keys.getInt(1);
                                requestCharacteristic.setKey(key);
                            }
                        }
                    }
                }
            }
            return requestCharacteristic;
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
    }

    public static void deleteRequestCharacteristic(RequestCharacteristic requestCharacteristic) throws DataException {
        try {
            if (requestCharacteristic.getKey() != null && requestCharacteristic.getKey() > 0) { //delete
                try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(dRequestCharacteristic)) {
                    ps.setInt(1, requestCharacteristic.getKey());
                    if (ps.executeUpdate() == 0) {
                        throw new OptimisticLockException(requestCharacteristic);
                    }
                }
            }
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
    }
}