package com.guflimc.config.toml;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import com.guflimc.config.common.Config;
import com.guflimc.config.common.ConfigComment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.nio.file.Path;

public class TomlConfig implements Config {

    private final static Logger LOGGER = LoggerFactory.getLogger(TomlConfig.class);

    private final static TomlConfig INSTANCE = new TomlConfig();

    private TomlConfig () {}

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
        TomlParser parser = new TomlParser();
        com.electronwill.nightconfig.core.Config config = parser.parse(contents);
        ObjectConverter converter = new ObjectConverter();
        converter.toObject(config, obj);
        return obj;
    }

    @Override
    public <T> String write(T obj) {
        CommentedConfig config = from(obj);

        TomlWriter toml = new TomlWriter();
        toml.setIndent("");

        return toml.writeToString(config);
    }

    //

    private <T> CommentedConfig from(T obj) {
        CommentedConfig config = CommentedConfig.of(TomlFormat.instance());
        ObjectConverter converter = new ObjectConverter();
        converter.toConfig(obj, config);

        try {
            recursiveSetComments(obj, config);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return config;
    }

    private void recursiveSetComments(Object object, CommentedConfig config) throws NoSuchFieldException, IllegalAccessException {
        for (CommentedConfig.Entry entry : config.entrySet() ) {
            Class<?> cls = object.getClass();
            Field field = null;
            while ( field == null ) {
                try {
                    field = cls.getDeclaredField(entry.getKey());
                } catch (NoSuchFieldException e) {
                    cls = cls.getSuperclass();
                }
            }
            field.setAccessible(true);

            ConfigComment ann = field.getAnnotation(ConfigComment.class);
            if ( ann != null ) {
                entry.setComment(" " + ann.value().trim().replace("\n", "\n "));
            }

            if ( entry.getValue() instanceof CommentedConfig cc ) {
                recursiveSetComments(field.get(object), cc);
            }
        }
    }

}
