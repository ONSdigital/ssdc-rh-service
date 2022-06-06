package uk.gov.ons.ssdc.rhservice.config;

import lombok.Data;

// @Configuration
// @ConfigurationProperties("cloud-storage.backoff")
// TODO Real config & sensible figures
@Data
public class FireStoreRetryConfig {
  private int initial = 1;
  private String multiplier = "1.0"; // String type to handle floats without rounding
  private int max = 20;
  private int maxAttempts = 10;
}
