package com.hcmus.mentor.backend.service;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
  void init();

  void save(MultipartFile file);

  Resource load(String filename);

  void delete(String fileName);

  void deleteAll();

  Stream<Path> loadAll();
}
