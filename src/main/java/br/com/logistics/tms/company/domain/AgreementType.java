package br.com.logistics.tms.company.domain;

public enum AgreementType {
    SELLS_ON,
    DELIVERS_WITH,
    OFFERS_PICKUP,
    OPERATES;

    public static AgreementType with(final String type) {
        return AgreementType.valueOf(type);
    }
}