package org.openremote.beta.server.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonUtil {

    public static final ObjectMapper JSON = new ObjectMapper()
        .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
        .configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

}
