package it.univaq.framework.jackson;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import it.univaq.example.webshop.model.Notification;
import it.univaq.example.webshop.model.NotificationTypeEnum;
import it.univaq.example.webshop.model.User;
import java.time.LocalDateTime;

public class NotificationDeserializer extends JsonDeserializer<Notification> {

    @Override
    public Notification deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        Notification f = new Notification();
        
        JsonNode node = jp.getCodec().readTree(jp);

        if (node.has("id")) {
            f.setKey(node.get("id").asInt());
        }

        if (node.has("destinatario")) {
            f.setRecipient(jp.getCodec().treeToValue(node.get("destinatario"), User.class));
        }

        if (node.has("messaggio")) {
            f.setMessage(node.get("messaggio").asText());
        }

        if (node.has("link")) {
            f.setLink(node.get("link").asText());
        }

        if (node.has("tipo")) {
            f.setType(NotificationTypeEnum.valueOf(node.get("tipo").asText()));
        }

        if (node.has("data_creazione")) {
            f.setCreationDate(jp.getCodec().treeToValue(node.get("data_creazione"), LocalDateTime.class));
        }

        if (node.has("letto")) {
            f.setRead(node.get("letto").asBoolean());
        }
        
        return f;
    }
}