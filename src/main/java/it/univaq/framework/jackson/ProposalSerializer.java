package it.univaq.framework.jackson;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import it.univaq.example.webshop.model.Proposal;

public class ProposalSerializer extends JsonSerializer<Proposal> {

    @Override
    public void serialize(Proposal item, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {

        jgen.writeStartObject(); // {
        jgen.writeNumberField("id", item.getKey());
        jgen.writeObjectField("richiesta", item.getRequest());
        jgen.writeObjectField("tecnico", item.getTechnician());
        jgen.writeStringField("nome_prodotto", item.getProductName());
        jgen.writeStringField("nome_produttore", item.getProducerName());
        jgen.writeStringField("descrizione_prodotto", item.getProductDescription());
        jgen.writeNumberField("prezzo_prodotto", item.getProductPrice());
        jgen.writeStringField("url", item.getUrl());
        jgen.writeStringField("note", item.getNotes());
        String dateAsString = item.getCreationDate().format(DateTimeFormatter.ofPattern("d/M/yyyy  HH:mm"));
        jgen.writeStringField("data_creazione", dateAsString);
        jgen.writeStringField("stato_proposta", item.getProposalState().toString());
        jgen.writeStringField("motivazione", item.getMotivation());
        jgen.writeEndObject(); // }
    }
}