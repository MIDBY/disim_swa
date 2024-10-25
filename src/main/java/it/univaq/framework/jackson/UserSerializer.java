package it.univaq.framework.jackson;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import it.univaq.example.webshop.model.User;
import java.time.format.DateTimeFormatter;


public class UserSerializer extends JsonSerializer<User> {

    @Override
    public void serialize(User item, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {

        jgen.writeStartObject(); // {
        jgen.writeNumberField("id", item.getKey());
        jgen.writeStringField("username", item.getUsername());
        jgen.writeStringField("email", item.getEmail());
        jgen.writeStringField("password", item.getPassword());
        jgen.writeStringField("indirizzo", item.getAddress());
        String dateAsString = item.getSubscriptionDate().format(DateTimeFormatter.ofPattern("d/M/yyyy"));
        jgen.writeStringField("data_iscrizione", dateAsString);
        jgen.writeBooleanField("accettato", item.isAccepted());
        jgen.writeNumberField("versione", item.getVersion());
        jgen.writeObjectField("notifiche", item.getNotifications());
        jgen.writeEndObject(); // }
    }
}