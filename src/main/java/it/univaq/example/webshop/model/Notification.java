package it.univaq.example.webshop.model;

import java.time.LocalDateTime;
import it.univaq.example.webshop.model.Notification;
import it.univaq.framework.data.DataItemImpl;

public class Notification extends DataItemImpl<Integer> {

    private User recipient;
    private String message;
    private String link;
    private NotificationTypeEnum type;
    private LocalDateTime creationDate;
    private boolean read;

    public Notification() {
        super();
        recipient = null;
        message = "";
        link = "";
        type = NotificationTypeEnum.INFO;
        creationDate = LocalDateTime.now();
        read = false;
    }

    public User getRecipient() {
        return recipient;
    }

    public void setRecipient(User recipient) {
        this.recipient = recipient;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public NotificationTypeEnum getType() {
        return type;
    }
    
    public void setType(NotificationTypeEnum value) {
        this.type = value;
    }
    
    public LocalDateTime getCreationDate() {
        return creationDate;
    }
    
    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public boolean isRead() {
        return read;
    }
    
    public void setRead(boolean read) {
        this.read = read;
    }
}
