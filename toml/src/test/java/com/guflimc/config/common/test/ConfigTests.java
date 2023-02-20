package com.guflimc.config.common.test;

import com.guflimc.config.common.ConfigComment;
import com.guflimc.config.toml.TomlConfig;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigTests {

    private static class ConfigObj {

        public boolean a = true;
        public int b = 5;

        @ConfigComment("This explains this section.")
        public InnterConfigObj c = new InnterConfigObj();

    }

    private static class InnterConfigObj {

        @ConfigComment("More explanation.")
        public List<String> d = List.of("e", "f", "g");

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
        ConfigObj obj = TomlConfig.get().read(modified, new ConfigObj());

        assertFalse(obj.a);
        assertEquals(5, obj.b);
        assertNotNull(obj.c);
        assertEquals(2, obj.c.d.size());
        assertEquals(obj.c.d, List.of("k", "l"));
    }

}
