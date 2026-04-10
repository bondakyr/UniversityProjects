package cz.cvut.fel.omo.smartfactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import cz.cvut.fel.omo.smartfactory.config.JsonConfiguration;

import java.io.File;
import java.io.IOException;

public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public static JsonConfiguration loadJsonConfiguration(String filePath) throws IOException {
        return objectMapper.readValue(new File(filePath), JsonConfiguration.class);
    }
}
