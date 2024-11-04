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
        try (OutputStream os = new FileOutputStream(imageFilename);
                InputStream inputStream = is) { // If 'is' is not already a resource, you might want to manage its lifecycle
    
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) > 0) {
                os.write(buffer, 0, read);
            }
        } catch (FileNotFoundException ex) {
            throw new DataException("Error storing image file", ex);
        } catch (IOException ex) {
            throw new DataException("Error storing image file", ex);
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