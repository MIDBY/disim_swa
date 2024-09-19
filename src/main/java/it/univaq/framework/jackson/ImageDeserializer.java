package it.univaq.framework.jackson;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import it.univaq.example.webshop.model.Image;

public class ImageDeserializer extends JsonDeserializer<Image> {

    @Override
    public Image deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
                Image f = new Image();
        
        JsonNode node = jp.getCodec().readTree(jp);

        if (node.has("id")) {
            f.setKey(node.get("id").asInt());
        }

        if (node.has("nome_file")) {
            f.setFilename(node.get("nome_file").asText());
        }

        if (node.has("didascalia")) {
            f.setCaption(node.get("didascalia").asText());
        }

        if (node.has("tipo_immagine")) {
            f.setImageType(node.get("tipo_immagine").asText());
        }

        if (node.has("grandezza_immagine")) {
            f.setImageSize(node.get("grandezza_immagine").asLong());
        }

        return f;
    }
}