package uk.gov.ons.ssdc.rhservice.service;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.UacOr4xxResponseEntity;
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

  public UacOr4xxResponseEntity getUac(String uacHash) throws RuntimeException {
    Optional<UacUpdateDTO> uacOpt = uacRepository.readUAC(uacHash);
    UacOr4xxResponseEntity uacOr4xxResponseEntity = new UacOr4xxResponseEntity();

    if (uacOpt.isEmpty()) {
      uacOr4xxResponseEntity.setResponseEntityOptional(Optional.of(new ResponseEntity<>("UAC Not Found", HttpStatus.NOT_FOUND)));
      return uacOr4xxResponseEntity;
    }

    UacUpdateDTO uacUpdateDTO = uacOpt.get();

    if (uacUpdateDTO.isReceiptReceived()) {
      uacOr4xxResponseEntity.setResponseEntityOptional(Optional.of(new ResponseEntity<>("UAC_RECEIPTED", HttpStatus.BAD_REQUEST)));
      return uacOr4xxResponseEntity;
    }

    if (!uacUpdateDTO.isActive()) {
      uacOr4xxResponseEntity.setResponseEntityOptional(Optional.of(new ResponseEntity<>("UAC_INACTIVE", HttpStatus.BAD_REQUEST)));
      return uacOr4xxResponseEntity;
    }

    uacOr4xxResponseEntity.setUacUpdateDTO(uacUpdateDTO);
    uacOr4xxResponseEntity.setCaseUpdateDTO(getCaseFromUac(uacUpdateDTO));

    return uacOr4xxResponseEntity;
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
