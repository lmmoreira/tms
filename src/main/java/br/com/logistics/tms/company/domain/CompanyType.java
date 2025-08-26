package br.com.logistics.tms.company.domain;

public enum CompanyType {
    MARKETPLACE,
    SELLER,
    PUDO,
    CROSS_DOCKING,
    LOGISTICS_PROVIDER;

    public static CompanyType with(final String type) {
        return CompanyType.valueOf(type);
    }
}