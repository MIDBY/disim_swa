package it.univaq.framework.data;

public interface DataItemProxy {

    boolean isModified();

    void setModified(boolean dirty);

}