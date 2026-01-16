package com.gufli.config.common;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public interface Config {

    // READ

    <T> T read(String contents, T config);

    default <T> T read(Path path, T config) throws IOException {
        if ( !Files.exists(path) ) {
            write(path, config);
            return config;
        }

        return read(Files.readString(path), config);
    }

    default <T> T read(InputStream inputStream, T config) throws IOException {
        try (
                InputStreamReader reader = new InputStreamReader(inputStream);
        ) {
            return read(reader, config);
        }
    }

    default <T> T read(Reader reader, T config) throws IOException {
        try (
                BufferedReader bufferedReader = new BufferedReader(reader);
        ) {
            String contents = bufferedReader.lines().collect(Collectors.joining(""));
            return read(contents, config);
        }
    }

    // WRITE

    <T> String write(T config);

    default <T> void write(Path path, T config) throws IOException {
        Files.createDirectories(path.getParent());

        if ( !Files.exists(path) ) {
            Files.createFile(path);
        }

        Files.writeString(path, write(config));
    }

    default <T> void write(OutputStream outputStream, T config) throws IOException {
        try (
                OutputStreamWriter writer = new OutputStreamWriter(outputStream);
        ) {
            writer.write(write(config));
        }
    }

}
