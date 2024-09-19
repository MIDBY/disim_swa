package it.univaq.example.webshop.model;

import it.univaq.example.webshop.model.RequestCharacteristic;
import it.univaq.framework.data.DataItemImpl;

public class RequestCharacteristic extends DataItemImpl<Integer> {

    private Request request;
    private Characteristic characteristic;
    private String value;

    public RequestCharacteristic() {
        super();
        request = new Request();
        characteristic = new Characteristic();
        value = "";
    }
    
    public Request getRequest() {
        return request;
    }
    
    public void setRequest(Request request) {
        this.request = request;
    }
    
    public Characteristic getCharacteristic() {
        return characteristic;
    }
    
    public void setCharacteristic(Characteristic characteristic) {
        this.characteristic = characteristic;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
}
