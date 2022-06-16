package uk.gov.ons.ssdc.rhservice.model;

import lombok.Data;
import uk.gov.ons.ssdc.rhservice.utils.Language;

import java.io.Serializable;

@Data
public class EqLaunchRequestDTO implements Serializable {
    private Language languageCode;
    private String accountServiceUrl;
    private String accountServiceLogoutUrl;
    private String clientIP;
}
