package it.univaq.example.webshop.business;

import it.univaq.example.webshop.model.Category;
import it.univaq.example.webshop.model.Image;
import it.univaq.framework.data.DAO;
import it.univaq.framework.data.DataException;
import it.univaq.framework.data.DataItemProxy;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import it.univaq.framework.data.DataLayer;
import it.univaq.framework.data.OptimisticLockException;

public class ImageDAO extends DAO {

    private PreparedStatement sImageByID, sImages, sImageByCategory, iImage, uImage, dImage;

    public ImageDAO(DataLayer d) {
        super(d);
    }

    
    public void init() throws DataException {
        try {
            super.init();

            //precompiliamo tutte le query utilizzate nella classe
            //precompile all the queries uses in this class
            sImageByID = connection.prepareStatement("SELECT * FROM immagine WHERE id=?");
            sImages = connection.prepareStatement("SELECT * FROM immagine");
            sImageByCategory = connection.prepareStatement("SELECT id_immagine FROM categoria WHERE id=?");
            iImage = connection.prepareStatement("INSERT INTO immagine (titolo,tipo,nomeFile,grandezza) VALUES(?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            uImage = connection.prepareStatement("UPDATE immagine SET titolo=?,tipo=?,nomeFile=?,grandezza=?,versione=? WHERE id=? and versione=?");
            dImage = connection.prepareStatement("DELETE FROM immagine WHERE id=?");

        } catch (SQLException ex) {
            throw new DataException("Error initializing webshop data layer", ex);
        }
    }

    
    public void destroy() throws DataException {
        try {
            sImageByID.close();
            sImages.close();
            sImageByCategory.close();
            iImage.close();
            uImage.close();
            dImage.close();

        } catch (SQLException ex) {
            ex.getStackTrace();
        }
        super.destroy();
    }

    
    public Image createImage() {
        return new Image();
    }

    //helper    
    private Image createImage(ResultSet rs) throws DataException {
        Image i = (Image)createImage();
        try {
            i.setKey(rs.getInt("id"));
            i.setCaption(rs.getString("titolo"));
            i.setImageSize(rs.getLong("grandezza"));
            i.setImageType(rs.getString("tipo"));
            i.setFilename(rs.getString("nomeFile"));
            i.setVersion(rs.getLong("versione"));
        } catch (SQLException ex) {
            throw new DataException("Unable to create image object form ResultSet", ex);
        }
        return i;
    }

    
    public Image getImage(int image_key) throws DataException {
        Image i = null;
        if (dataLayer.getCache().has(Image.class, image_key)) {
            i = dataLayer.getCache().get(Image.class, image_key);
        } else {
            try {
                sImageByID.clearParameters();
                sImageByID.setInt(1, image_key);
                try (ResultSet rs = sImageByID.executeQuery()) {
                    if (rs.next()) {
                        i = createImage(rs);
                        dataLayer.getCache().add(Image.class, i);

                    }
                }
            } catch (SQLException ex) {
                throw new DataException("Unable to load image by ID", ex);
            }
        }
        return i;
    }

    
    public List<Image> getImages() throws DataException {
        List<Image> result = new ArrayList<>();
        try (ResultSet rs = sImages.executeQuery()) {
            while (rs.next()) {
                result.add(getImage(rs.getInt("id")));
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load images", ex);
        }
        return result;
    }

    
    public Image getImageByCategory(Category category) throws DataException {
        Image result = new Image();
        try {
            sImageByCategory.setInt(1, category.getKey());
            try (ResultSet rs = sImageByCategory.executeQuery()) {
                while(rs.next()) {
                    result = getImage(rs.getInt("id_immagine"));
                }
            }
        } catch (SQLException e) {
            throw new DataException("Unable to load image by category", e);
        }
        return result;
    }

    
    public void setImage(Image image) throws DataException {
                try {
            if (image.getKey() != null && image.getKey() > 0) { //update
                if (image instanceof DataItemProxy && !((DataItemProxy) image).isModified()) {
                    return;
                }
                uImage.setString(1, image.getCaption());
                uImage.setString(2, image.getImageType());
                uImage.setString(3, image.getFilename());
                uImage.setLong(4, image.getImageSize());

                long current_version = image.getVersion();
                long next_version = current_version + 1;

                uImage.setLong(5, next_version);
                uImage.setInt(6, image.getKey());
                uImage.setLong(7, current_version);

                if (uImage.executeUpdate() == 0) {
                    throw new OptimisticLockException(image);
                } else {
                    image.setVersion(next_version);
                }
            } else { //insert
                iImage.setString(1, image.getCaption());
                iImage.setString(2, image.getImageType());
                iImage.setString(3, image.getFilename());
                iImage.setLong(4, image.getImageSize());

                if (iImage.executeUpdate() == 1) {
                    try ( ResultSet keys = iImage.getGeneratedKeys()) {
                        if (keys.next()) {
                            int key = keys.getInt(1);
                            image.setKey(key);
                            dataLayer.getCache().add(Image.class, image);
                        }
                    }
                }
            }

            if (image instanceof DataItemProxy) {
                ((DataItemProxy) image).setModified(false);
            }
        } catch (SQLException | OptimisticLockException ex) {
            throw new DataException("Unable to store image", ex);
        }
    }

    
    public void deleteImage(Image image) throws DataException {
        try {
            if (image.getKey() != null && image.getKey() > 0) { //delete
                dImage.setInt(1, image.getKey());
                if (dImage.executeUpdate() == 0) {
                    throw new OptimisticLockException(image);
                } else {
                    dataLayer.getCache().delete(Image.class, image);
                }
            }
        } catch (SQLException | OptimisticLockException ex) {
            throw new DataException("Unable to delete image", ex);
        }
    }
}