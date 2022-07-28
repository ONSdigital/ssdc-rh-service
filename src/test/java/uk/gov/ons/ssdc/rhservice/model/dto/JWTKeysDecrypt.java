package uk.gov.ons.ssdc.rhservice.model.dto;

import lombok.Data;

@Data
public class JWTKeysDecrypt {
  Key jwsPublicKey;
  Key jwePrivateKey;
}
