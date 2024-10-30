package br.com.logistics.tms.commons.infrastructure.json;

public interface JsonAdapter {

    String toJson(Object object);

    <T> T fromJson(String json, Class<T> clazz);

}
