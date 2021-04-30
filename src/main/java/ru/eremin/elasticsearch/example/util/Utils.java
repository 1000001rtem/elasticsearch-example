package ru.eremin.elasticsearch.example.util;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Utils {

    public static String readResource(String alias) {
        try {
            Path path = Paths.get(new ClassPathResource(alias).getURI());
            return Files.readString(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
