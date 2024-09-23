package it.univaq.example.webshop.base;

import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import it.univaq.example.webshop.resources.CategoriesResource;
import it.univaq.example.webshop.resources.CategoryResource;
import it.univaq.example.webshop.resources.CharacteristicResource;
import it.univaq.example.webshop.resources.CharacteristicsResource;
import it.univaq.example.webshop.resources.FattureResource;
import it.univaq.example.webshop.resources.GroupResource;
import it.univaq.example.webshop.resources.ImageResource;
import it.univaq.example.webshop.resources.ImagesResource;
import it.univaq.example.webshop.resources.NotificationResource;
import it.univaq.example.webshop.resources.NotificationsResource;
import it.univaq.example.webshop.resources.ProdottiResource;
import it.univaq.example.webshop.resources.ProposalResource;
import it.univaq.example.webshop.resources.ProposalsResource;
import it.univaq.example.webshop.resources.RequestCharacteristicResource;
import it.univaq.example.webshop.resources.RequestCharacteristicsResource;
import it.univaq.example.webshop.resources.RequestResource;
import it.univaq.example.webshop.resources.RequestsResource;
import it.univaq.example.webshop.resources.ServiceResource;
import it.univaq.example.webshop.resources.UsersResource;
import it.univaq.framework.jackson.ObjectMapperContextResolver;
import it.univaq.framework.security.AuthLoggedFilter;
import it.univaq.framework.security.AuthenticationRes;
import it.univaq.framework.security.CORSFilter;
/**
 *
 * @author didattica
 */
@ApplicationPath("rest")
public class RESTApp extends Application {

    private final Set<Class<?>> classes;

    public RESTApp() {
        HashSet<Class<?>> c = new HashSet<>();
        //aggiungiamo tutte le *root resurces* (cioè quelle
        //con l'annotazione Path) che vogliamo pubblicare
        c.add(CategoryResource.class);
        c.add(CategoriesResource.class);
        c.add(CharacteristicResource.class);
        c.add(CharacteristicsResource.class);
        c.add(GroupResource.class);
        c.add(ImageResource.class);
        c.add(ImagesResource.class);
        c.add(NotificationResource.class);
        c.add(NotificationsResource.class);
        c.add(ProposalResource.class);
        c.add(ProposalsResource.class);
        c.add(RequestCharacteristicResource.class);
        c.add(RequestCharacteristicsResource.class);
        c.add(RequestResource.class);
        c.add(RequestsResource.class);
        c.add(UsersResource.class);
        c.add(UsersResource.class);
        c.add(ServiceResource.class);

        c.add(FattureResource.class);
        c.add(ProdottiResource.class);
        c.add(AuthenticationRes.class);

        //aggiungiamo il provider Jackson per poter
        //usare i suoi servizi di serializzazione e 
        //deserializzazione JSON
        c.add(JacksonJsonProvider.class);

        //necessario se vogliamo una (de)serializzazione custom di qualche classe    
        c.add(ObjectMapperContextResolver.class);

        //esempio di autenticazione
        c.add(AuthLoggedFilter.class);

        //aggiungiamo il filtro che gestisce gli header CORS
        c.add(CORSFilter.class);

        classes = Collections.unmodifiableSet(c);
    }

    //l'override di questo metodo deve restituire il set
    //di classi che Jersey utilizzerà per pubblicare il
    //servizio. Tutte le altre, anche se annotate, verranno
    //IGNORATE
    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }
}