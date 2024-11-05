package it.univaq.example.webshop.model;

public enum ProposalStateEnum {
    INATTESA, APPROVATO, RESPINTO;

    public static boolean isValidEnumValue(String value) {
        for (ProposalStateEnum enumValue : ProposalStateEnum.values()) {
            if (enumValue.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
