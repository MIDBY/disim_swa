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
import it.univaq.example.webshop.model.Category;
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

    public static Category createCategory() {
        return new Category();
    }

    private static Category createCategory(ResultSet rs) throws RESTWebApplicationException {
        try {
            Category a = createCategory();
            a.setKey(rs.getInt("id"));
            a.setName(rs.getString("nome"));
            a.setFatherCategory(getCategory(rs.getInt("idCategoriaPadre")));
            a.setImage(ImageResourceDB.getImage(rs.getInt("idImmagine")));
            a.setDeleted(rs.getBoolean("eliminato"));
            a.setVersion(rs.getLong("versione"));
            return a;
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("Unable to create category object form ResultSet: " + ex.getMessage());
        }
    }

    public static Category getCategory(int category_key) throws RESTWebApplicationException {
        Category l = null;
        try {
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sCategoryByID)) {
                ps.setInt(1, category_key);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        l = createCategory(rs);
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

    
    public static List<Category> getFatherCategories() throws RESTWebApplicationException {
        try {
            List<Category> l = new ArrayList<>();
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sFatherCategories)) {
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        l.add(createCategory(rs));
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
        
    public static List<Category> getCategories() throws RESTWebApplicationException {
        try {
            List<Category> l = new ArrayList<>();
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sCategories)) {
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        l.add(createCategory(rs));
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

    public static List<Category> getCategoriesByDeleted(boolean deleted) throws RESTWebApplicationException {
        List<Category> result = new ArrayList<>();
        try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sCategoriesByDeleted)) {
            ps.setBoolean(1, deleted);
            try ( ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(getCategory(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
        return result;
    }
    
    public static List<Category> getCategoriesSonsOf(int category_key) throws RESTWebApplicationException {
        List<Category> result = new ArrayList<>();
        try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sCategoriesSonsOf)) {
            ps.setInt(1, category_key);
            try ( ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(getCategory(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
        return result;
    }

    public static List<Category> getMostSoldCategories() throws RESTWebApplicationException {
        List<Category> result = new ArrayList<>();
        try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sMostSoldCategories)) {
            try ( ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(getCategory(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
        return result;
    }

    public static List<Category> getCategoriesByImage(int image_key) throws RESTWebApplicationException {
        List<Category> result = new ArrayList<>();
        try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sCategoryByImage)) {
            ps.setInt(1, image_key);
            try ( ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(getCategory(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
        return result;
    }

    public static Category setCategory(Category category) throws RESTWebApplicationException {
        try {
            if (category.getKey() != null && category.getKey() > 0) { //update
                try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(uCategory)) {
                    ps.setString(1, category.getName());
                    if (category.getFatherCategory() != null) {
                        ps.setInt(2, category.getFatherCategory().getKey());
                    } else {
                        ps.setNull(2, java.sql.Types.INTEGER);
                    }                
                    if (category.getImage() != null) {
                        ps.setInt(3, category.getImage().getKey());
                    } else {
                        ps.setNull(3, java.sql.Types.INTEGER);
                    }       
                    ps.setBoolean(4, category.isDeleted());

                    long current_version = category.getVersion();
                    long next_version = current_version + 1;

                    ps.setLong(5, next_version);
                    ps.setInt(6, category.getKey());
                    ps.setLong(7, current_version);

                    if (ps.executeUpdate() == 0) {
                        throw new RESTWebApplicationException("Category not updated");
                    } else {
                        category.setVersion(next_version);
                    }
                }
            } else { //insert
                try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(iCategory)) {
                    ps.setString(1, category.getName());
                    if (category.getFatherCategory() != null) {
                        ps.setInt(2, category.getFatherCategory().getKey());
                    } else {
                        ps.setNull(2, java.sql.Types.INTEGER);
                    }
                    if (category.getImage() != null) {
                        ps.setInt(3, category.getImage().getKey());
                    } else {
                        ps.setNull(3, java.sql.Types.INTEGER);
                    }
                    if (ps.executeUpdate() == 1) {
                        try ( ResultSet keys = ps.getGeneratedKeys()) {
                            if (keys.next()) {
                                int key = keys.getInt(1);
                                category.setKey(key);
                            }
                        }
                    }
                }
            }
            return category;
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
    }

    public static void deleteCategory(Category category) throws DataException {
        try {
            if (category.getKey() != null && category.getKey() > 0) { //delete
                try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(dCategory)) {
                    ps.setInt(1, category.getKey());
                    if (ps.executeUpdate() == 0) {
                        throw new OptimisticLockException(category);
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