package it.univaq.framework.jackson;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import it.univaq.example.webshop.model.User;

public class UserSerializer extends JsonSerializer<User> {

    @Override
    public void serialize(User item, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {

        jgen.writeStartObject(); // {
        jgen.writeNumberField("id", item.getKey());
        jgen.writeStringField("username", item.getUsername());
        jgen.writeStringField("email", item.getEmail());
        jgen.writeStringField("indirizzo", item.getAddress());
        jgen.writeObjectField("data_iscrizione", item.getSubscriptionDate());
        jgen.writeBooleanField("accettato", item.isAccepted());
        jgen.writeObjectField("notifiche", item.getNotifications());
        jgen.writeEndObject(); // }
    }
}