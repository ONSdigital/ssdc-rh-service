package uk.gov.ons.ssdc.rhservice.service;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
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

  public Optional<UacUpdateDTO> getUac(String uacHash) throws RuntimeException {
    return uacRepository.readUAC(uacHash);
  }

  public CaseUpdateDTO getCaseFromUac(UacUpdateDTO uacUpdateDTO) {

    String caseId = uacUpdateDTO.getCaseId();
    if (StringUtils.isEmpty(caseId)) {
      throw new RuntimeException("UAC has no caseId");
    }

    return caseRepository
        .readCaseUpdate(caseId)
        .orElseThrow(() -> new RuntimeException("Case Not Found for UAC"));
  }
}
