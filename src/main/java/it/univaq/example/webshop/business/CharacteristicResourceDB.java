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
import it.univaq.example.webshop.model.Characteristic;
import it.univaq.framework.data.DataException;
import it.univaq.framework.data.OptimisticLockException;
import it.univaq.framework.exceptions.RESTWebApplicationException;

public class CharacteristicResourceDB {

    private static final String DS_NAME = "java:comp/env/jdbc/webshopdb";
    private static final String sCharacteristicByID = "SELECT * FROM caratteristica WHERE id=?";
    private static final String sCharacteristicsByCategory = "SELECT id FROM caratteristica WHERE idCategoria=?";
    private static final String sCharacteristics = "SELECT id FROM caratteristica";
    private static final String iCharacteristic = "INSERT INTO caratteristica (nome,idCategoria,valoriDefault) VALUES(?,?,?)";
    private static final String uCharacteristic = "UPDATE caratteristica SET nome=?,idCategoria=?,valoriDefault=?,versione=? WHERE id=? and versione=?";
    private static final String dCharacteristic = "DELETE FROM caratteristica WHERE id=?";

    private static Connection getPooledConnection() throws NamingException, SQLException {
        InitialContext ctx = new InitialContext();
        DataSource ds = (DataSource) ctx.lookup(DS_NAME);
        return ds.getConnection();
    }


    public static Characteristic createCharacteristic() {
        return new Characteristic();
    }

    private static Characteristic createCharacteristic(ResultSet rs) throws RESTWebApplicationException {
        try {
            Characteristic a = createCharacteristic();
            a.setKey(rs.getInt("id"));
            a.setName(rs.getString("nome"));
            a.setCategory(CategoryResourceDB.getCategory(rs.getInt("idCategoria")));
            a.setDefaultValues(rs.getString("valoriDefault"));
            a.setVersion(rs.getLong("versione"));
            return a;
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("Unable to create characteristic object form ResultSet: " + ex.getMessage());
        }
    }

    public static Characteristic getCharacteristic(int characteristic_key) throws RESTWebApplicationException {
        Characteristic l = null;
        try {
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sCharacteristicByID)) {
                ps.setInt(1, characteristic_key);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        l = createCharacteristic(rs);
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

    
    public static List<Characteristic> getCharacteristicsByCategory(int category_key) throws RESTWebApplicationException {
        try {
            List<Characteristic> l = new ArrayList<>();
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sCharacteristicsByCategory)) {
                ps.setInt(1, category_key);
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        l.add(getCharacteristic(rs.getInt("id")));
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
        
    public static List<Characteristic> getCharacteristics() throws RESTWebApplicationException {
        try {
            List<Characteristic> l = new ArrayList<>();
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sCharacteristics)) {
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        l.add(getCharacteristic(rs.getInt("id")));
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

    public static Characteristic setCharacteristic(Characteristic characteristic) throws RESTWebApplicationException {
        try {
            if (characteristic.getKey() != null && characteristic.getKey() > 0) { //update
                try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(uCharacteristic)) {
                    ps.setString(1, characteristic.getName());
                    if (characteristic.getCategory() != null) {
                        ps.setInt(2, characteristic.getCategory().getKey());
                    } else {
                        ps.setNull(2, java.sql.Types.INTEGER);
                    }                
                    ps.setString(3, characteristic.getDefaultValues());

                    long current_version = characteristic.getVersion();
                    long next_version = current_version + 1;

                    ps.setLong(4, next_version);
                    ps.setInt(5, characteristic.getKey());
                    ps.setLong(6, current_version);

                    if (ps.executeUpdate() == 0) {
                        throw new RESTWebApplicationException("Characteristic not updated");
                    } else {
                        characteristic.setVersion(next_version);
                    }
                }
            } else { //insert
                try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(iCharacteristic)) {
                    ps.setString(1, characteristic.getName());
                    if (characteristic.getCategory() != null) {
                        ps.setInt(2, characteristic.getCategory().getKey());
                    } else {
                        ps.setNull(2, java.sql.Types.INTEGER);
                    }
                    ps.setString(3, characteristic.getDefaultValues());

                    if (ps.executeUpdate() == 1) {
                        try ( ResultSet keys = ps.getGeneratedKeys()) {
                            if (keys.next()) {
                                int key = keys.getInt(1);
                                characteristic.setKey(key);
                            }
                        }
                    }
                }
            }
            return characteristic;
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
    }

    public static void deleteCharacteristic(Characteristic characteristic) throws DataException {
        try {
            if (characteristic.getKey() != null && characteristic.getKey() > 0) { //delete
                try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(dCharacteristic)) {
                    ps.setInt(1, characteristic.getKey());
                    if (ps.executeUpdate() == 0) {
                        throw new OptimisticLockException(characteristic);
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