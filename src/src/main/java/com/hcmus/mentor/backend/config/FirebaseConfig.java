package com.hcmus.mentor.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class FirebaseConfig {

    private static final String FIREBASE_ADMIN_CREDENTIALS = "/mentorus-firebase-admins.json";
    private static final String FIREBASE_APP_NAME = "mentor-hcmus";

    @Bean
    FirebaseMessaging firebaseMessaging() throws IOException {
        var googleCredentials = GoogleCredentials.fromStream(new ClassPathResource(FIREBASE_ADMIN_CREDENTIALS).getInputStream());
        FirebaseOptions firebaseOptions = FirebaseOptions.builder()
                .setCredentials(googleCredentials)
                .build();

        FirebaseApp app;
        if (FirebaseApp.getApps().isEmpty()) {
            app = FirebaseApp.initializeApp(firebaseOptions, FIREBASE_APP_NAME);
        } else {
            app = FirebaseApp.getInstance(FIREBASE_APP_NAME);
        }

        return FirebaseMessaging.getInstance(app);
    }
}
