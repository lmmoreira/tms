package br.com.logistics.tms.company.domain;

public enum AgreementConditionType {
    USES_PROVIDER,
    DISCOUNT_PERCENTAGE,
    DELIVERY_SLA_DAYS;

    public static AgreementConditionType with(final String type) {
        return AgreementConditionType.valueOf(type);
    }
}