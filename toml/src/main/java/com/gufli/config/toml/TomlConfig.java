package com.gufli.config.toml;

import com.gufli.config.common.Config;
import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.document.TomlDocument;
import io.github.wasabithumb.jtoml.option.JTomlOption;
import io.github.wasabithumb.jtoml.option.JTomlOptions;
import io.github.wasabithumb.jtoml.option.prop.IndentationPolicy;
import io.github.wasabithumb.jtoml.option.prop.LineSeparator;
import io.github.wasabithumb.jtoml.serial.TomlSerializable;
import io.github.wasabithumb.jtoml.serial.reflect.Key;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.nio.file.Path;

public class TomlConfig implements Config {

    private final static Logger LOGGER = LoggerFactory.getLogger(TomlConfig.class);

    private final static TomlConfig INSTANCE = new TomlConfig();

    private final static JToml jtoml = JToml.jToml(JTomlOptions.builder()
            .set(JTomlOption.LINE_SEPARATOR, LineSeparator.LF)
            .set(JTomlOption.INDENTATION, IndentationPolicy.NONE)
            .build());

    private TomlConfig() {
    }

    public static TomlConfig get() {
        return INSTANCE;
    }

    //

    public static <T> T load(Path path, T config) {
        try {
            config = get().read(path, config);
        } catch (Exception e) {
            LOGGER.error("Invalid config file. More information:");
            LOGGER.error(e.getMessage());
        }
        return config;
    }

    //

    @Override
    public <T> T read(String contents, T obj) {
        TomlDocument document = jtoml.readFromString(contents);
        return jtoml.fromToml((Class<T>) obj.getClass(), document.asTable());
    }

    @Override
    public <T> String write(T obj) {
        TomlTable table = this.serialize(obj);
        return jtoml.writeToString(table);
    }

    private TomlTable serialize(Object obj) {
        TomlTable table = jtoml.toToml(obj);

        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (!TomlSerializable.class.isAssignableFrom(field.getType())) {
                continue;
            }

            try {
                String name = field.getName();

                Key key = field.getAnnotation(Key.class);
                if (key != null) {
                    name = key.value();
                }

                TomlTable fieldTable = serialize(field.get(obj));
                table.put(name, fieldTable);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return table;
    }

}
