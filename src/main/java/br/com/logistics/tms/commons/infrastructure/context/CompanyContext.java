package br.com.logistics.tms.commons.infrastructure.context;

public class CompanyContext {

    private static final ThreadLocal<Company> currentCompany = new ThreadLocal<>();

    public static Company getCurrentCompany() {
        return currentCompany.get();
    }

    public static void setCurrentCompany(final Company company) {
        currentCompany.set(company);
    }

    public static void clear() {
        currentCompany.remove();
    }

}