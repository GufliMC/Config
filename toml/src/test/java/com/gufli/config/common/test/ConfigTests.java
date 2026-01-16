package com.gufli.config.common.test;

import com.gufli.config.common.ConfigComment;
import com.gufli.config.toml.TomlConfig;
import io.github.wasabithumb.jtoml.comment.Comment;
import io.github.wasabithumb.jtoml.serial.TomlSerializable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigTests {

    public static class ConfigObj implements TomlSerializable {

        @Comment.Pre("This explains this section.")
        public boolean a = true;
        public int b = 5;

        public InnterConfigObj c = new InnterConfigObj();

        public ConfigObj() {
            c.d = List.of("x", "y", "z");
        }

    }

    public static class InnterConfigObj implements TomlSerializable {

        @Comment.Pre("More explanation.")
        public List<String> d = List.of("e", "f", "g");

    }

    @TempDir
    Path tempDir;

    Path tempFile;

    @BeforeEach
    public void setUp() {
        tempFile = tempDir.resolve("config.toml");
    }

    @Test
    public void writeTest() throws IOException {
        String contents = TomlConfig.get().write(new ConfigObj());
        String test = new String(getClass().getClassLoader().getResourceAsStream("default.toml").readAllBytes());
        assertEquals(test, contents);
    }

    @Test
    public void readTest() throws IOException {
        String modified = new String(getClass().getClassLoader().getResourceAsStream("modified.toml").readAllBytes());
        ConfigObj config = TomlConfig.get().read(modified, new ConfigObj());

        assertFalse(config.a);
        assertEquals(5, config.b);
        assertNotNull(config.c);
        assertEquals(2, config.c.d.size());
        assertEquals(config.c.d, List.of("k", "l"));
    }

    @Test
    public void loadTest() {
        assertFalse(Files.exists(tempFile));
        ConfigObj def = new ConfigObj();

        // first => create
        ConfigObj config = TomlConfig.load(tempFile, new ConfigObj());
        assertTrue(Files.exists(tempFile));
        assertEquals(def.a, config.a);
        assertEquals(def.b, config.b);
        assertEquals(def.c.d, config.c.d);

        // second => only read
        config = TomlConfig.load(tempFile, new ConfigObj());
        assertEquals(def.a, config.a);
        assertEquals(def.b, config.b);
        assertEquals(def.c.d, config.c.d);
    }

}
