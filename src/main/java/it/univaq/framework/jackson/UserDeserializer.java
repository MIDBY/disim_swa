package it.univaq.framework.jackson;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import it.univaq.example.webshop.model.Notification;
import it.univaq.example.webshop.model.User;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.time.format.DateTimeFormatter;

public class UserDeserializer extends JsonDeserializer<User> {

    @Override
    public User deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        User f = new User();
        
        JsonNode node = jp.getCodec().readTree(jp);

        if (node.has("id")) {
            f.setKey(node.get("id").asInt());
        }

        if (node.has("username")) {
            f.setUsername(node.get("username").asText());
        }

        if (node.has("email")) {
            f.setEmail(node.get("email").asText());
        }

        if (node.has("indirizzo")) {
            f.setEmail(node.get("indirizzo").asText());
        }

        if (node.has("data_iscrizione")) {
            LocalDate ld = LocalDate.parse(node.get("data_iscrizione").asText(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));      
            f.setSubscriptionDate(ld);
        }

        if (node.has("accettato")) {
            f.setAccepted(node.get("accettato").asBoolean());
        }

        if (node.has("notifiche")) {
            JsonNode ne = node.get("notifiche");
            List<Notification> notifiche = new ArrayList<>();
            for (int i = 0; i < ne.size(); ++i) {
                notifiche.add(jp.getCodec().treeToValue(ne.get(i), Notification.class));
            }  
            f.setNotifications(notifiche);
        }
        
        return f;
    }
}