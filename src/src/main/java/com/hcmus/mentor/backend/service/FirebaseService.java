package com.hcmus.mentor.backend.service;

import java.util.List;
import java.util.Map;

public interface FirebaseService {

    void sendNotificationMulticast(List<String> receiverIds, String title, String content, Map<String, String> data);
}
