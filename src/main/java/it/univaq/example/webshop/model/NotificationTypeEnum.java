package it.univaq.example.webshop.model;

public enum NotificationTypeEnum {
    INFO, NUOVO, MODIFICATO, CHIUSO, ANNULLATO;


    public static boolean isValidEnumValue(String value) {
        for (NotificationTypeEnum enumValue : NotificationTypeEnum.values()) {
            if (enumValue.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
