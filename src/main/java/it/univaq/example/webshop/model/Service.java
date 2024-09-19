package it.univaq.example.webshop.model;

import it.univaq.example.webshop.model.Service;
import it.univaq.framework.data.DataItemImpl;

public class Service extends DataItemImpl<Integer> {
    
    private String script;

    public Service() {
        super();
        script = "";
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }
}
