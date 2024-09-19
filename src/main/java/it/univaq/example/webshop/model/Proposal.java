package it.univaq.example.webshop.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import it.univaq.example.webshop.model.Proposal;
import it.univaq.framework.data.DataItemImpl;

public class Proposal extends DataItemImpl<Integer> {

    private Request request;
    private User technician;
    private String productName;
    private String producerName;
    private String productDescription;
    private Float productPrice;
    private String url;
    private String notes;
    private LocalDateTime creationDate;
    private ProposalStateEnum proposalState;
    private String motivation;

    public Proposal() {
        super();
        request = null;
        technician = null;
        productName = "";
        producerName = "";
        productDescription = "";
        productPrice = 0.00F;
        url = "";
        notes = "";
        creationDate = LocalDateTime.now();
        proposalState = ProposalStateEnum.INATTESA;
        motivation = "";
    }

    public Request getRequest() {
        return request;
    }
    
    public void setRequest(Request request) {
        this.request = request;
    }
    
    public User getTechnician() {
        return technician;
    }

    public void setTechnician(User techUser) {
        technician = techUser;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProducerName() {
        return producerName;
    }
    
    public void setProducerName(String producerName) {
        this.producerName = producerName;
    }

    public String getProductDescription() {
        return productDescription;
    }
    
    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }
    
    public Float getProductPrice() {
        BigDecimal bd = new BigDecimal(productPrice);
        return bd.setScale(2, RoundingMode.HALF_EVEN).floatValue();
    }
    
    public void setProductPrice(Float price) {
        BigDecimal bd = new BigDecimal(price);
        this.productPrice = bd.setScale(2, RoundingMode.HALF_EVEN).floatValue();
    }
    
    public String getUrl() {
        return url;
    }
    
    public boolean setUrl(String url) {
        if(url.length() > 0) {
            try {
                URL u = new URL(url);
                u.toURI();
            } catch (MalformedURLException|URISyntaxException e) {
                e.printStackTrace();
                return false;
            }
        }
        this.url = url;
        return true;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getCreationDate() {
        return creationDate;
    }
    
    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }
    
    public ProposalStateEnum getProposalState() {
        return proposalState;
    }
    
    public void setProposalState(ProposalStateEnum value) {
        proposalState = value;
    }
    
    public String getMotivation() {
        return motivation;
    }
    
    public void setMotivation(String motivation) {
        this.motivation = motivation;
    }
}
