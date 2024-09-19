package it.univaq.example.webshop.model;

import it.univaq.example.webshop.model.Image;
import it.univaq.framework.data.DataException;
import it.univaq.framework.data.DataItemImpl;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Image  extends DataItemImpl<Integer> {

    private String caption;
    private String imageType;
    private String imageFilename;
    private long imageSize;
  

    public Image() {
        super();
        caption = "";
        imageSize = 0;
        imageType = "";
        imageFilename = "";
        
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
        
    }

    public String getFilename() {
        return imageFilename;
    }

    public void setFilename(String imageFilename) {
        this.imageFilename = imageFilename;
        
    }
    
    public InputStream getImageData() throws DataException {
        try {
            return new FileInputStream(imageFilename);
        } catch (FileNotFoundException ex) {
            throw new DataException("Error opening image file", ex);
        }
    }

    public void setImageData(InputStream is) throws DataException {

        OutputStream os = null;
        try {
            byte[] buffer = new byte[1024];
            os = new FileOutputStream(imageFilename);
            int read;
            while ((read = is.read(buffer)) > 0) {
                os.write(buffer, 0, read);
            }
        } catch (FileNotFoundException ex) {
            throw new DataException("Error storing image file", ex);
        } catch (IOException ex) {
            throw new DataException("Error storing image file", ex);
        } finally {
            try {
                os.close();
            } catch (IOException ex) {
                Logger.getLogger(Image.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String type) {
        this.imageType = type;
        
    }

    public long getImageSize() {
        return imageSize;
    }

    public void setImageSize(long size) {
        this.imageSize = size;
    }
}