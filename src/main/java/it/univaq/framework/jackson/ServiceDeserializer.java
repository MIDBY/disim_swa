package it.univaq.framework.jackson;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import it.univaq.example.webshop.model.Service;

public class ServiceDeserializer extends JsonDeserializer<Service> {

    @Override
    public Service deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        Service f = new Service();
        
        JsonNode node = jp.getCodec().readTree(jp);

        if (node.has("id")) {
            f.setKey(node.get("id").asInt());
        }

        if (node.has("script")) {
            f.setScript(node.get("script").asText());
        }

        return f;
    }
}