package uk.gov.ons.ssdc.rhservice.service;

import lombok.Data;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;


@Data
public class EqLaunchData extends EqLaunchCoreData {
    private CaseUpdateDTO caseUpdate;
    private String userId;
    private String accountServiceUrl;
    private String accountServiceLogoutUrl;
}
