package br.com.logistics.tms.company.domain;

public enum AgreementConditionType {
    USES_PROVIDER;

    public static AgreementConditionType with(final String type) {
        return AgreementConditionType.valueOf(type);
    }
}