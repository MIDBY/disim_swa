package it.univaq.framework.jackson;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import it.univaq.example.webshop.model.Request;

public class RequestSerializer extends JsonSerializer<Request> {

    @Override
    public void serialize(Request item, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {

        jgen.writeStartObject(); // {
        jgen.writeNumberField("id", item.getKey());
        jgen.writeStringField("titolo", item.getTitle());
        jgen.writeStringField("descrizione", item.getDescription());
        jgen.writeObjectField("categoria", item.getCategory());
        jgen.writeObjectField("ordinante", item.getOrdering());
        jgen.writeObjectField("tecnico", item.getTechnician());
        jgen.writeObjectField("data_creazione", item.getCreationDate());
        jgen.writeStringField("stato_richiesta", item.getRequestState().toString());
        jgen.writeStringField("stato_ordine", item.getOrderState().toString());
        jgen.writeObjectField("caratteristiche", item.getRequestCharacteristics());
        jgen.writeObjectField("proposte", item.getProposals());
        jgen.writeStringField("note", item.getNotes());
        jgen.writeEndObject(); // }
    }
}