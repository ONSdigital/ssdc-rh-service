package uk.gov.ons.ssdc.rhservice.model.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.ons.ssdc.rhservice.utils.Language;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder(toBuilder = true)
@Data
public class EqLaunchCoreData {
  private Language language;
  private String salt;
  private String source;
  private String channel;

  //  TODO: can we remove this?
  public EqLaunchCoreData coreCopy() {
    return this.toBuilder().build();
  }
}
