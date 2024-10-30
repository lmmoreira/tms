package br.com.logistics.tms.company.domain;

public enum Type {
    MARKETPLACE,
    SELLER,
    PUDO,
    LOGISTICS_PROVIDER;

    public static Type with(String type) {
        return Type.valueOf(type);
    }
}