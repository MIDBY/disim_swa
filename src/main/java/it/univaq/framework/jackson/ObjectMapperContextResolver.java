package it.univaq.framework.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import it.univaq.example.webshop.model.Category;
import it.univaq.example.webshop.model.Characteristic;
import it.univaq.example.webshop.model.Group;
import it.univaq.example.webshop.model.Image;
import it.univaq.example.webshop.model.Notification;
import it.univaq.example.webshop.model.Proposal;
import it.univaq.example.webshop.model.Request;
import it.univaq.example.webshop.model.RequestCharacteristic;
import it.univaq.example.webshop.model.Service;
import it.univaq.example.webshop.model.User;

import java.util.Calendar;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

/**
 *
 * @author didattica
 */
@Provider
public class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {

    private final ObjectMapper mapper;

    public ObjectMapperContextResolver() {
        this.mapper = createObjectMapper();
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        //abilitiamo una feature nuova...
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        SimpleModule customSerializer = new SimpleModule("CustomSerializersModule");

        //configuriamo i nostri serializzatori custom
        customSerializer.addSerializer(Calendar.class, new JavaCalendarSerializer());
        customSerializer.addDeserializer(Calendar.class, new JavaCalendarDeserializer());
        //
        customSerializer.addSerializer(User.class, new UserSerializer());
        customSerializer.addDeserializer(User.class, new UserDeserializer());
        //
        customSerializer.addSerializer(Group.class, new GroupSerializer());
        customSerializer.addDeserializer(Group.class, new GroupDeserializer());
        //
        customSerializer.addSerializer(Service.class, new ServiceSerializer());
        customSerializer.addDeserializer(Service.class, new ServiceDeserializer());
        //
        customSerializer.addSerializer(Notification.class, new NotificationSerializer());
        customSerializer.addDeserializer(Notification.class, new NotificationDeserializer());
        //
        customSerializer.addSerializer(Category.class, new CategorySerializer());
        customSerializer.addDeserializer(Category.class, new CategoryDeserializer());
        //
        customSerializer.addSerializer(Characteristic.class, new CharacteristicSerializer());
        customSerializer.addDeserializer(Characteristic.class, new CharacteristicDeserializer());
        //
        customSerializer.addSerializer(Image.class, new ImageSerializer());
        customSerializer.addDeserializer(Image.class, new ImageDeserializer());
        //
        customSerializer.addSerializer(RequestCharacteristic.class, new RequestCharacteristicSerializer());
        customSerializer.addDeserializer(RequestCharacteristic.class, new RequestCharacteristicDeserializer());
        //
        customSerializer.addSerializer(Request.class, new RequestSerializer());
        customSerializer.addDeserializer(Request.class, new RequestDeserializer());
        //
        customSerializer.addSerializer(Proposal.class, new ProposalSerializer());
        customSerializer.addDeserializer(Proposal.class, new ProposalDeserializer());
        //
        mapper.registerModule(customSerializer);

        //per il supporto alla serializzazione automatica dei tipi Date/Time di Java 8 (LocalDate, LocalTime, ecc.)
        //è necessario aggiungere alle dipendenze la libreria com.fasterxml.jackson.datatype:jackson-datatype-jsr310
        //questa feature fa cercare a Jackson tutti i moduli compatibili inseriti nel contesto...
        mapper.findAndRegisterModules();

        return mapper;
    }
}