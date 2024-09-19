package it.univaq.framework.jackson;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import it.univaq.example.webshop.model.Category;
import it.univaq.example.webshop.model.Characteristic;

public class CharacteristicDeserializer extends JsonDeserializer<Characteristic> {

    @Override
    public Characteristic deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        Characteristic f = new Characteristic();
        
        JsonNode node = jp.getCodec().readTree(jp);

        if (node.has("id")) {
            f.setKey(node.get("id").asInt());
        }

        if (node.has("nome")) {
            f.setName(node.get("nome").asText());
        }

        if (node.has("categoria")) {
            f.setCategory(jp.getCodec().treeToValue(node.get("categoria"), Category.class));
        }

        if (node.has("valori_default")) {
            f.setDefaultValues(node.get("valori_default").asText());
        }
        
        return f;
    }
}