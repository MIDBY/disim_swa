package it.univaq.framework.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import it.univaq.example.webshop.model2.Anagrafica;
import it.univaq.example.webshop.model2.Fattura;
import it.univaq.example.webshop.model2.Prodotto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FatturaDeserializer extends JsonDeserializer<Fattura> {

    @Override
    public Fattura deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        Fattura f = new Fattura();
        
        JsonNode node = jp.getCodec().readTree(jp);

        if (node.has("numero")) {
            f.setNumero(node.get("numero").asInt());
        }

        if (node.has("data")) {
            f.setData(jp.getCodec().treeToValue(node.get("data"), LocalDate.class));
        }
        if (node.has("intestatario")) {
            f.setIntestatario(jp.getCodec().treeToValue(node.get("intestatario"), Anagrafica.class));
        }

        if (node.has("elementi")) {
            JsonNode ne = node.get("elementi");
            List<Prodotto> elementi = new ArrayList<>();
            f.setElementi(elementi);
            for (int i = 0; i < ne.size(); ++i) {
                elementi.add(jp.getCodec().treeToValue(ne.get(i), Prodotto.class));
            }           
        }
        if (node.has("totali")) {
            JsonNode nt = node.get("totali");
            if (nt.has("totaleIVAEsclusa")) {
                f.setTotaleIVAEsclusa(nt.get("totaleIVAEsclusa").asDouble());
            }
            if (nt.has("totaleIVA")) {
                f.setTotaleIVA(nt.get("totaleIVA").asDouble());
            }
            if (nt.has("totaleIVAInclusa")) {
                f.setTotaleIVAInclusa(nt.get("totaleIVAInclusa").asDouble());
            }
        }

        return f;
    }
}