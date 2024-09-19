package it.univaq.framework.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import it.univaq.example.webshop.model2.Fattura;

public class FatturaSerializer extends JsonSerializer<Fattura> {

    @Override
    public void serialize(Fattura item, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {

        jgen.writeStartObject(); // {
        jgen.writeNumberField("numero", item.getNumero()); // "numero": 1
        jgen.writeObjectField("data", item.getData()); // "data": [2020,5,4]
        jgen.writeObjectField("intestatario", item.getIntestatario());
        jgen.writeObjectField("elementi", item.getElementi());
        jgen.writeObjectFieldStart("totali"); // "totali": {
        jgen.writeNumberField("totaleIVAEsclusa", item.getTotaleIVAEsclusa());
        jgen.writeNumberField("totaleIVA", item.getTotaleIVA());
        jgen.writeNumberField("totaleIVAInclusa", item.getTotaleIVAInclusa());
        jgen.writeEndObject(); // }
        jgen.writeEndObject(); // }
    }
}