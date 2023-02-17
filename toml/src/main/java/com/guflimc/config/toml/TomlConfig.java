package com.guflimc.config.toml;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import com.guflimc.config.common.Config;
import com.guflimc.config.common.ConfigComment;

import java.io.File;
import java.lang.reflect.Field;

public class TomlConfig implements Config {

    private final static TomlConfig INSTANCE = new TomlConfig();

    private TomlConfig () {}

    public static TomlConfig get() {
        return INSTANCE;
    }

    //

    public static <T> T load(File file, T config) {
        return load(file, config, true);
    }

    public static <T> T load(File file, T config, boolean update) {
        try {
            config = get().read(file, config);
            if ( update ) {
                get().write(file, config);
            }
        } catch (Exception e) {
            System.err.println("Invalid config file. Please fix your formatting. ");
            System.err.println(e.getMessage());
        }
        return config;
    }

    //

    @Override
    public <T> String write(T obj) {
        com.electronwill.nightconfig.core.CommentedConfig config = com.electronwill.nightconfig.core.CommentedConfig.of(TomlFormat.instance());
        ObjectConverter converter = new ObjectConverter();
        converter.toConfig(obj, config);

        try {
            recursiveSetComments(obj, config);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        TomlWriter toml = new TomlWriter();
        return toml.writeToString(config);
    }

    private void recursiveSetComments(Object object, CommentedConfig config) throws NoSuchFieldException, IllegalAccessException {
        for (CommentedConfig.Entry entry : config.entrySet() ) {
            Field field = object.getClass().getDeclaredField(entry.getKey());
            field.setAccessible(true);

            ConfigComment ann = field.getAnnotation(ConfigComment.class);
            if ( ann != null ) {
                entry.setComment(ann.value());
            }

            if ( entry.getValue() instanceof CommentedConfig cc ) {
                recursiveSetComments(field.get(object), cc);
            }
        }
    }

    @Override
    public <T> T read(String contents, T obj) {
        ConfigParser<CommentedConfig> parser = new TomlParser();
        com.electronwill.nightconfig.core.Config config = parser.parse(contents);
        ObjectConverter converter = new ObjectConverter();
        converter.toObject(config, obj);
        return obj;
    }
}
