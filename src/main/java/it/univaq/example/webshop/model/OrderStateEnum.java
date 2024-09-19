package it.univaq.example.webshop.model;

public enum OrderStateEnum {
    SPEDITO, ACCETTATO, RESPINTONONCONFORME, RESPINTONONFUNZIONANTE, EMPTY;

    @Override
    public String toString() {
        return this == EMPTY ? "" : this.name();
    }
}
