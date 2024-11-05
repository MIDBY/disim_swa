package it.univaq.example.webshop.model;

public enum RequestStateEnum {
    NUOVO, PRESOINCARICO, ORDINATO, CHIUSO, ANNULLATO;

    public static boolean isValidEnumValue(String value) {
        for (RequestStateEnum enumValue : RequestStateEnum.values()) {
            if (enumValue.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
