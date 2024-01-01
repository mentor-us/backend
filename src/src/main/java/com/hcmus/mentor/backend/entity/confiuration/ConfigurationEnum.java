package com.hcmus.mentor.backend.entity.confiuration;

public enum ConfigurationEnum {
  SYSTEM_EMAIL;

  @Override
  public String toString() {
    switch (this) {
      case SYSTEM_EMAIL:
        return "system_email";
      default:
        return "";
    }
  }
}
