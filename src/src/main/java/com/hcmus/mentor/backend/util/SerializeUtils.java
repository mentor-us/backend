package com.hcmus.mentor.backend.util;

import org.springframework.util.SerializationUtils;

import javax.servlet.http.Cookie;
import java.util.Base64;

public class SerializeUtils {

    public static String serialize(Object object) {
        return Base64.getUrlEncoder()
                .encodeToString(SerializationUtils.serialize(object));
    }

    public static <T> T deserialize(Cookie cookie, Class<T> clazz) {
        byte[] data = Base64.getUrlDecoder().decode(cookie.getValue());
        return clazz.cast(SerializationUtils.deserialize(data));
    }
}
