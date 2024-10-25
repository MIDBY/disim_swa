package it.univaq.example.webshop.model;

public enum OrderStateEnum {
    SPEDITO, ACCETTATO, RESPINTONONCONFORME, RESPINTONONFUNZIONANTE, EMPTY;

    @Override
    public String toString() {
        return this == EMPTY ? "" : this.name();
    }

    public static boolean isValidEnumValue(String value) {
        for (NotificationTypeEnum enumValue : NotificationTypeEnum.values()) {
            if (enumValue.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
