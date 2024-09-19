package it.univaq.framework.jackson;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import it.univaq.example.webshop.model.Category;

public class CategorySerializer extends JsonSerializer<Category> {

    @Override
    public void serialize(Category item, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {

        jgen.writeStartObject(); // {
        jgen.writeNumberField("id", item.getKey());
        jgen.writeStringField("nome", item.getName());
        jgen.writeObjectField("immagine", item.getImage());
        jgen.writeObjectField("categoria_padre", item.getFatherCategory());
        jgen.writeObjectField("caratteristiche", item.getCharacteristics());
        jgen.writeEndObject(); // }
    }
}