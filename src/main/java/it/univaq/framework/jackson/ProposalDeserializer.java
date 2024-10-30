package it.univaq.framework.jackson;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import it.univaq.example.webshop.model.Proposal;
import it.univaq.example.webshop.model.ProposalStateEnum;
import it.univaq.example.webshop.model.Request;
import it.univaq.example.webshop.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProposalDeserializer extends JsonDeserializer<Proposal> {

    @Override
    public Proposal deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        Proposal f = new Proposal();
        
        JsonNode node = jp.getCodec().readTree(jp);

        if (node.has("id")) {
            f.setKey(node.get("id").asInt());
        }

        if (node.has("richiesta")) {
            f.setRequest(jp.getCodec().treeToValue(node.get("richiesta"), Request.class));
        }

        if (node.has("tecnico")) {
            f.setTechnician(jp.getCodec().treeToValue(node.get("tecnico"), User.class));
        }

        if (node.has("nome_prodotto")) {
            f.setProductName(node.get("nome_prodotto").asText());
        }

        if (node.has("nome_produttore")) {
            f.setProducerName(node.get("nome_produttore").asText());
        }

        if (node.has("descrizione_prodotto")) {
            f.setProductDescription(node.get("descrizione_prodotto").asText());
        }

        if (node.has("prezzo_prodotto")) {
            f.setProductPrice((float) node.get("prezzo_prodotto").asDouble());
        }

        if (node.has("url")) {
            f.setUrl(node.get("url").asText());
        }

        if (node.has("note")) {
            f.setNotes(node.get("note").asText());
        }

        if (node.has("data_creazione")) {
            f.setCreationDate(LocalDateTime.parse(node.get("data_creazione").asText(), DateTimeFormatter.ofPattern("d/M/yyyy  HH:mm")));
        }

        if (node.has("stato_proposta")) {
            f.setProposalState(ProposalStateEnum.valueOf(node.get("stato_proposta").asText()));
        }

        if (node.has("motivazione")) {
            f.setMotivation(node.get("motivazione").asText());
        }
        
        return f;
    }
}