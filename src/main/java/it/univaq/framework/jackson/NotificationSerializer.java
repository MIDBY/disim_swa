package it.univaq.framework.jackson;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import it.univaq.example.webshop.model.Notification;

public class NotificationSerializer extends JsonSerializer<Notification> {

    @Override
    public void serialize(Notification item, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {

        jgen.writeStartObject(); // {
        jgen.writeNumberField("id", item.getKey());
        jgen.writeObjectField("destinatario", item.getRecipient());
        jgen.writeStringField("messaggio", item.getMessage());
        jgen.writeStringField("link", item.getLink());
        jgen.writeStringField("tipo", item.getType().toString());
        jgen.writeObjectField("data_creazione", item.getCreationDate());
        jgen.writeBooleanField("letto", item.isRead());
        jgen.writeEndObject(); // }
    }
}