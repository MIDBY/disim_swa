package it.univaq.framework.jackson;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import it.univaq.example.webshop.model.Group;
import it.univaq.example.webshop.model.Service;
import it.univaq.example.webshop.model.User;
import it.univaq.example.webshop.model.UserRoleEnum;
import java.util.ArrayList;
import java.util.List;

public class GroupDeserializer extends JsonDeserializer<Group> {

    @Override
    public Group deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        Group f = new Group();
        
        JsonNode node = jp.getCodec().readTree(jp);

        if (node.has("id")) {
            f.setKey(node.get("id").asInt());
        }

        if (node.has("nome")) {
            f.setName(UserRoleEnum.valueOf(node.get("nome").asText()));
        }

        if (node.has("utenti")) {
            JsonNode ne = node.get("utenti");
            List<User> utenti = new ArrayList<>();
            for (int i = 0; i < ne.size(); ++i) {
                utenti.add(jp.getCodec().treeToValue(ne.get(i), User.class));
            }
            f.setUsers(utenti);
        }

        if (node.has("servizi")) {
            JsonNode ne = node.get("servizi");
            List<Service> servizi = new ArrayList<>();
            for (int i = 0; i < ne.size(); ++i) {
                servizi.add(jp.getCodec().treeToValue(ne.get(i), Service.class));
            }
            f.setServices(servizi);           
        }

        return f;
    }
}