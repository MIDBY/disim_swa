package it.univaq.framework.jackson;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import it.univaq.example.webshop.model.Characteristic;
import it.univaq.example.webshop.model.Request;
import it.univaq.example.webshop.model.RequestCharacteristic;

public class RequestCharacteristicDeserializer extends JsonDeserializer<RequestCharacteristic> {

    @Override
    public RequestCharacteristic deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        RequestCharacteristic f = new RequestCharacteristic();
        
        JsonNode node = jp.getCodec().readTree(jp);

        if (node.has("id")) {
            f.setKey(node.get("id").asInt());
        }

        if (node.has("richiesta")) {
            f.setRequest(jp.getCodec().treeToValue(node.get("richiesta"), Request.class));
        }

        if (node.has("caratteristica")) {
            f.setCharacteristic(jp.getCodec().treeToValue(node.get("caratteristica"), Characteristic.class));
        }
        
        if (node.has("valore")) {
            f.setValue(node.get("valore").asText());
        }
        
        return f;
    }
}