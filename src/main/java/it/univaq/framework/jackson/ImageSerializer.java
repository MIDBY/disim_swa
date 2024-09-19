package it.univaq.framework.jackson;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import it.univaq.example.webshop.model.Image;

public class ImageSerializer extends JsonSerializer<Image> {

    @Override
    public void serialize(Image item, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {

        jgen.writeStartObject(); // {
        jgen.writeNumberField("id", item.getKey());
        jgen.writeStringField("nome_file", item.getFilename());
        jgen.writeStringField("didascalia", item.getCaption());
        jgen.writeStringField("tipo_immagine", item.getImageType());
        jgen.writeNumberField("grandezza_immagine", item.getImageSize());
        jgen.writeEndObject(); // }
    }
}