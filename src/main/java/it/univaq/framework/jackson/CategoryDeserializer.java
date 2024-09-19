package it.univaq.framework.jackson;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import it.univaq.example.webshop.model.Category;
import it.univaq.example.webshop.model.Characteristic;
import it.univaq.example.webshop.model.Image;
import java.util.ArrayList;
import java.util.List;

public class CategoryDeserializer extends JsonDeserializer<Category> {

    @Override
    public Category deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        Category f = new Category();
        
        JsonNode node = jp.getCodec().readTree(jp);

        if (node.has("id")) {
            f.setKey(node.get("id").asInt());
        }

        if (node.has("nome")) {
            f.setName(node.get("nome").asText());
        }

        if (node.has("immagine")) {
            f.setImage(jp.getCodec().treeToValue(node.get("immagine"), Image.class));
        }

        if (node.has("categoria_padre")) {
            f.setFatherCategory(jp.getCodec().treeToValue(node.get("categoria_padre"), Category.class));
        }

        if (node.has("caratteristiche")) {
            JsonNode ne = node.get("caratteristiche");
            List<Characteristic> caratteristiche = new ArrayList<>();
            for (int i = 0; i < ne.size(); ++i) {
                caratteristiche.add(jp.getCodec().treeToValue(ne.get(i), Characteristic.class));
            }  
            f.setCharacteristics(caratteristiche);
        }
        
        return f;
    }
}