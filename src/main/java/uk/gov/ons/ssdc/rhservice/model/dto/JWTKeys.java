package uk.gov.ons.ssdc.rhservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class JWTKeys {
    Key jwePublicKey;
    Key jwsPrivateKey;
}
