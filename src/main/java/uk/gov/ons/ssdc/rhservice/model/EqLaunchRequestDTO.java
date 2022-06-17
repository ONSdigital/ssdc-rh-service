package uk.gov.ons.ssdc.rhservice.model;

import java.io.Serializable;
import lombok.Data;
import uk.gov.ons.ssdc.rhservice.utils.Language;

@Data
public class EqLaunchRequestDTO implements Serializable {
  private Language languageCode;
  private String accountServiceUrl;
  private String accountServiceLogoutUrl;
  private String clientIP;
}
