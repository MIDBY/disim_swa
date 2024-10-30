package it.univaq.framework.jackson;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import it.univaq.example.webshop.model.Category;
import it.univaq.example.webshop.model.OrderStateEnum;
import it.univaq.example.webshop.model.Proposal;
import it.univaq.example.webshop.model.Request;
import it.univaq.example.webshop.model.RequestCharacteristic;
import it.univaq.example.webshop.model.RequestStateEnum;
import it.univaq.example.webshop.model.User;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RequestDeserializer extends JsonDeserializer<Request> {

    @Override
    public Request deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        Request f = new Request();
        
        JsonNode node = jp.getCodec().readTree(jp);

        if (node.has("id")) {
            f.setKey(node.get("id").asInt());
        }

        if (node.has("descrizione")) {
            f.setDescription(node.get("descrizione").asText());
        }
        
        if (node.has("categoria")) {
            f.setCategory(jp.getCodec().treeToValue(node.get("categoria"), Category.class));
        }

        if (node.has("ordinante")) {
            f.setOrdering(jp.getCodec().treeToValue(node.get("ordinante"), User.class));
        }

        if (node.has("tecnico")) {
            f.setTechnician(jp.getCodec().treeToValue(node.get("tecnico"), User.class));
        }

        if (node.has("data_creazione")) {
            f.setCreationDate(LocalDate.parse(node.get("data_creazione").asText(), DateTimeFormatter.ofPattern("d/M/yyyy")));
        }

        if (node.has("stato_richiesta")) {
            f.setRequestState(RequestStateEnum.valueOf(node.get("stato_richiesta").asText()));
        }

        if (node.has("stato_ordine")) {
            if(node.get("stato_ordine").asText() == "")
                f.setOrderState(OrderStateEnum.EMPTY);
            else
                f.setOrderState(OrderStateEnum.valueOf(node.get("stato_ordine").asText()));
        }

        if (node.has("caratteristiche")) {
            JsonNode ne = node.get("caratteristiche");
            List<RequestCharacteristic> caratteristiche = new ArrayList<>();
            for (int i = 0; i < ne.size(); ++i) {
                caratteristiche.add(jp.getCodec().treeToValue(ne.get(i), RequestCharacteristic.class));
            }  
            f.setRequestCharacteristics(caratteristiche);
        }

        if (node.has("proposte")) {
            JsonNode ne = node.get("proposte");
            List<Proposal> proposte = new ArrayList<>();
            for (int i = 0; i < ne.size(); ++i) {
                proposte.add(jp.getCodec().treeToValue(ne.get(i), Proposal.class));
            }  
            f.setProposals(proposte);
        }

        if (node.has("note")) {
            f.setNotes(node.get("note").asText());
        }
        
        return f;
    }
}