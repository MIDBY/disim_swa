package it.univaq.framework.jackson;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import it.univaq.example.webshop.model.RequestCharacteristic;

public class RequestCharacteristicSerializer extends JsonSerializer<RequestCharacteristic> {

    @Override
    public void serialize(RequestCharacteristic item, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {

        jgen.writeStartObject(); // {
        jgen.writeNumberField("id", item.getKey());
        jgen.writeObjectField("richiesta", item.getRequest());
        jgen.writeObjectField("caratteristica", item.getCharacteristic());        
        jgen.writeStringField("valore", item.getValue());
        jgen.writeEndObject(); // }
    }
}