package br.com.logistics.tms.commons.infrastructure.jpa.repositories.sql;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SqlFileReader {

    private final static String RESOURCES_FILE_PATH = "src/main/resources/";

    public static String readSqlFile(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(RESOURCES_FILE_PATH.concat(filePath))));
        } catch (IOException e) {
            throw new SqlFileNotFoundException("Failed to read SQL file");
        }
    }
}