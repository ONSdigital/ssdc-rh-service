package uk.gov.ons.ssdc.rhservice.model.dto;

import lombok.Data;

@Data
public class EqLaunchData extends EqLaunchCoreData {
  private CaseUpdateDTO caseUpdate;
  private String userId;
  private String accountServiceUrl;
  private String accountServiceLogoutUrl;
}
