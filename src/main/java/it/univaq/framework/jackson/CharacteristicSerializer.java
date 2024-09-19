package it.univaq.framework.jackson;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import it.univaq.example.webshop.model.Characteristic;

public class CharacteristicSerializer extends JsonSerializer<Characteristic> {

    @Override
    public void serialize(Characteristic item, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {

        jgen.writeStartObject(); // {
        jgen.writeNumberField("id", item.getKey());
        jgen.writeStringField("nome", item.getName());
        jgen.writeObjectField("categoria", item.getCategory());
        jgen.writeStringField("valori_default", item.getDefaultValues());
        jgen.writeEndObject(); // }
    }
}