package it.univaq.example.webshop.model;

import java.util.List;
import it.univaq.example.webshop.model.Category;
import it.univaq.framework.data.DataItemImpl;

public class Category extends DataItemImpl<Integer> {

    private String name;
    private Image image;
    private Category fatherCategory;
    private List<Characteristic> characteristics;
    private boolean deleted;

    public Category() {
        super();
        name = "";
        image = null;
        fatherCategory = null;
        characteristics = null;
        deleted = false;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Image getImage() {
        return image;
    }
    
    public void setImage(Image image) {
        this.image = image;
    }
    
    public Category getFatherCategory() {
        return fatherCategory;
    }
    
    public void setFatherCategory(Category fathCategory) {
        fatherCategory = fathCategory;
    }
    
    public void setFatherCategoryNull() {
        fatherCategory = null;
    }
    
    public List<Characteristic> getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(List<Characteristic> characteristics) {
        this.characteristics = characteristics;
    }
    
    public void addCharacteristic(Characteristic characteristic) {
        characteristics.add(characteristic);
    }
    
    public boolean isDeleted() {
        return deleted;
    }
     
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
