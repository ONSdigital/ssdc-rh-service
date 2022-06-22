package uk.gov.ons.ssdc.rhservice.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.repository.CaseRepository;
import uk.gov.ons.ssdc.rhservice.model.repository.UacRepository;

@Service
public class UacService {
  private final UacRepository uacRepository;
  private final CaseRepository caseRepository;

  public UacService(UacRepository uacRepository, CaseRepository caseRepository) {
    this.uacRepository = uacRepository;
    this.caseRepository = caseRepository;
  }

  public void validateUacHash(String uacHash) throws RuntimeException {
    UacUpdateDTO uac =
        uacRepository
            .readUAC(uacHash)
            .orElseThrow(() -> new RuntimeException("Failed to retrieve UAC"));

    String caseId = uac.getCaseId();
    if (StringUtils.isEmpty(caseId)) {
      throw new RuntimeException("UAC has no caseId");
    }

    caseRepository
        .readCaseUpdate(caseId)
        .orElseThrow(() -> new RuntimeException("Case Not Found for UAC"));
  }
}
