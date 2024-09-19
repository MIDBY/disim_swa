package it.univaq.framework.jackson;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import it.univaq.example.webshop.model.Group;

public class GroupSerializer extends JsonSerializer<Group> {

    @Override
    public void serialize(Group item, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {

        jgen.writeStartObject(); // {
        jgen.writeNumberField("id", item.getKey());
        jgen.writeStringField("nome", item.getName().toString());
        jgen.writeObjectField("utenti", item.getUsers());
        jgen.writeObjectField("servizi", item.getServices());
        jgen.writeEndObject(); // }
    }
}