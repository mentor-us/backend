package com.hcmus.mentor.backend.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class RequestUtils {

    private final static Logger LOGGER = LogManager.getLogger(RequestUtils.class);

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    public static String toSlug(String input) {
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }

    public static String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        String bearerHeader = "Bearer ";
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(bearerHeader)) {
            String token = bearerToken.substring(bearerHeader.length());
            return token;
        }
        return null;
    }
}
