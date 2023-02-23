package com.guflimc.config.common;

import java.io.*;

public interface Config {

    // WRITE

    <T> String write(T config);

    default <T> void write(File file, T config) throws IOException {
        if ( !file.getParentFile().exists() && !file.getParentFile().mkdirs() ) {
            throw new IOException("Could not create parent directories for file: " + file.getAbsolutePath());
        }
        try (
                FileWriter writer = new FileWriter(file);
        ) {
            write(writer, config);
        }
    }

    default <T> void write(OutputStream outputStream, T config) throws IOException {
        try (
                OutputStreamWriter writer = new OutputStreamWriter(outputStream);
        ) {
            write(writer, config);
        }
    }

    default <T> void write(Writer writer, T config) throws IOException {
        try (
                BufferedWriter bufferedWriter = new BufferedWriter(writer);
        ) {
            bufferedWriter.write(write(config));
        }
    }

    // READ

    <T> T read(String contents, T config);

    default <T> T read(File file, T config) throws IOException {
        if ( !file.exists() ) {
            write(file, config);
            return config;
        }

        try (
                FileReader reader = new FileReader(file);
        ) {
            return read(reader, config);
        }
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
            StringBuilder contents = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                contents.append(line);
            }
            return read(contents.toString(), config);
        }
    }
}
