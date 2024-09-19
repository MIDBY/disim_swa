package it.univaq.example.webshop.model;

import it.univaq.example.webshop.model.Characteristic;
import it.univaq.framework.data.DataItemImpl;

public class Characteristic extends DataItemImpl<Integer> {

    private String name;
    private Category category;
    private String defaultValues;

    public Characteristic() {
        super();
        name = "";
        category = null;
        defaultValues = "";
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Category getCategory(){
        return category;
    }
    
    public void setCategory(Category category){
        this.category = category;
    }
    
    public String getDefaultValues() {
        return defaultValues;
    }
    
    public void setDefaultValues(String values) {
        defaultValues = values;
    }
}
