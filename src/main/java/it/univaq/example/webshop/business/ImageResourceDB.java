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
import it.univaq.example.webshop.model.Image;
import it.univaq.framework.data.DataException;
import it.univaq.framework.data.OptimisticLockException;
import it.univaq.framework.exceptions.RESTWebApplicationException;

public class ImageResourceDB {

    private static final String DS_NAME = "java:comp/env/jdbc/webshopdb";
    private static final String SQL_SELECT_ALL = "SELECT * FROM immagine";
    private static final String sImageByID = "SELECT * FROM immagine WHERE id=?";
    private static final String sImageByCategory = "SELECT id_immagine FROM categoria WHERE id=?";
    private static final String iImage = "INSERT INTO immagine (titolo,tipo,nomeFile,grandezza) VALUES(?,?,?,?)";
    private static final String uImage = "UPDATE immagine SET titolo=?,tipo=?,nomeFile=?,grandezza=?,versione=? WHERE id=? and versione=?";
    private static final String dImage = "DELETE FROM immagine WHERE id=?";

    private static Connection getPooledConnection() throws NamingException, SQLException {
        InitialContext ctx = new InitialContext();
        DataSource ds = (DataSource) ctx.lookup(DS_NAME);
        return ds.getConnection();
    }
    public static Image createImage() {
        return new Image();
    }

    private static Image createImage(ResultSet rs) throws RESTWebApplicationException {
        try {
            Image i = createImage();
            i.setKey(rs.getInt("id"));
            i.setCaption(rs.getString("titolo"));
            i.setImageSize(rs.getLong("grandezza"));
            i.setImageType(rs.getString("tipo"));
            i.setFilename(rs.getString("nomeFile"));
            i.setVersion(rs.getLong("versione"));
            return i;
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("Unable to create user object form ResultSet: " + ex.getMessage());
        }
    }

    public static Image getImage(int image_key) throws RESTWebApplicationException {
        Image l = null;
        try {
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sImageByID)) {
                ps.setInt(1, image_key);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        l = createImage(rs);
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

    public static List<Image> getImages() throws RESTWebApplicationException {
        try {
            List<Image> l = new ArrayList<>();
            try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(SQL_SELECT_ALL)) {
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        l.add(getImage(rs.getInt("id")));
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
    
    public static Image getImageByCategory(int category_key) throws RESTWebApplicationException {
        try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(sImageByCategory)) {
            ps.setInt(1, category_key);
            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return getImage(rs.getInt("id_immagine"));
                }
            }
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
        return null;
    }

    public static Image setImage(Image image) throws RESTWebApplicationException {
        try {
            if (image.getKey() != null && image.getKey() > 0) { //update
                try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(uImage)) {
                    ps.setString(1, image.getCaption());
                    ps.setString(2, image.getImageType());
                    ps.setString(3, image.getFilename());
                    ps.setLong(4, image.getImageSize());

                    long current_version = image.getVersion();
                    long next_version = current_version + 1;

                    ps.setLong(5, next_version);
                    ps.setInt(6, image.getKey());
                    ps.setLong(7, current_version);

                    if (ps.executeUpdate() == 0) {
                        throw new RESTWebApplicationException("Image not updated");
                    } else {
                        image.setVersion(next_version);
                    }
                }
            } else { //insert
                try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(iImage, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, image.getCaption());
                    ps.setString(2, image.getImageType());
                    ps.setString(3, image.getFilename());
                    ps.setLong(4, image.getImageSize());
    
                    if (ps.executeUpdate() == 1) {
                        try ( ResultSet keys = ps.getGeneratedKeys()) {
                            if (keys.next()) {
                                int key = keys.getInt(1);
                                image.setKey(key);
                            }
                        }
                    }
                }
            }
            return image;
        } catch (SQLException ex) {
            throw new RESTWebApplicationException("SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            throw new RESTWebApplicationException("DB: " + ex.getMessage());
        }
    }

    
    public static void deleteImage(Image image) throws DataException {
        try {
            if (image.getKey() != null && image.getKey() > 0) { //delete
                try ( Connection connection = getPooledConnection();  PreparedStatement ps = connection.prepareStatement(dImage)) {
                    ps.setInt(1, image.getKey());
                    if (ps.executeUpdate() == 0) {
                        throw new OptimisticLockException(image);
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