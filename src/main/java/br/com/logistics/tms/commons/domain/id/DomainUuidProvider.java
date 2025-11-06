package br.com.logistics.tms.commons.domain.id;

public class DomainUuidProvider {

    private static UuidAdapter uuidAdapter;

    private DomainUuidProvider() {}

    public static void setUuidAdapter(UuidAdapter instance) {
        uuidAdapter = instance;
    }

    public static UuidAdapter getUuidAdapter() {
        if (uuidAdapter == null) {
            throw new IllegalStateException("UuidAdapter not initialized");
        }
        return uuidAdapter;
    }

}
