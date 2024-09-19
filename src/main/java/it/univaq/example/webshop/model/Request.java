package it.univaq.example.webshop.model;

import java.time.LocalDate;
import java.util.List;
import it.univaq.example.webshop.model.Request;
import it.univaq.framework.data.DataItemImpl;

public class Request extends DataItemImpl<Integer> {
    
    private String title;
    private String description;
    private Category category;
    private User ordering;
    private User technician;
    private LocalDate creationDate;
    private RequestStateEnum requestState;
    private OrderStateEnum orderState;
    private List<RequestCharacteristic> characteristics;
    private List<Proposal> proposals;
    private String notes;

    public Request() {
        super();
        title = "";
        description = "";
        category = null;
        ordering = null;
        technician = null;
        creationDate = LocalDate.now();
        requestState = RequestStateEnum.NUOVO;
        orderState = OrderStateEnum.EMPTY;
        characteristics = null;
        proposals = null;
        notes = "";
    }
    
    public String getTitle() {
       return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
       this.description = description;
    }
    
    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
    
    public User getOrdering() {
        return ordering;
    }
    
    public void setOrdering(User orderingUser) {
        ordering = orderingUser;
    }

    public User getTechnician() {
        return technician;
    }
    
    public void setTechnician(User techUser) {
        technician = techUser;
    }
    
    public LocalDate getCreationDate() {
        return creationDate;
    }
    
    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }
    
    public RequestStateEnum getRequestState() {
        return requestState;
    }

    public void setRequestState(RequestStateEnum value) {
        requestState = value;
    }
    
    public OrderStateEnum getOrderState() {
        return orderState;
    }
    
    public void setOrderState(OrderStateEnum value) {
        orderState = value;
    }
    
    public List<RequestCharacteristic> getRequestCharacteristics() {
        return characteristics;
    }
    
    public void setRequestCharacteristics(List<RequestCharacteristic> requestCharacteristics) {
        characteristics = requestCharacteristics;
    }
    
    public void addRequestCharacteristic(RequestCharacteristic requestCharacteristic) {
        this.characteristics.add(requestCharacteristic);
    }
    
    public List<Proposal> getProposals() {
        return proposals;
    }
    
    public void setProposals(List<Proposal> proposals) {
        this.proposals = proposals;
    }

    public void addProposal(Proposal proposal) {
        proposals.add(proposal);
    }
    
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }    
}
