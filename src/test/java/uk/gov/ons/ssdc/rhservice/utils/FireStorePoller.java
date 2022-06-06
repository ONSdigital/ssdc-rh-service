package uk.gov.ons.ssdc.rhservice.utils;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.ons.ssdc.rhservice.exceptions.CaseNotFoundException;
import uk.gov.ons.ssdc.rhservice.exceptions.UacNotFoundException;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.repository.CaseRepository;
import uk.gov.ons.ssdc.rhservice.model.repository.UacRepository;

@Component
@ActiveProfiles("test")
public class FireStorePoller {
  @Autowired private CaseRepository caseRepository;
  @Autowired private UacRepository uacRepository;

  @Retryable(
      value = {CaseNotFoundException.class},
      maxAttempts = 5,
      backoff = @Backoff(delay = 1000))
  public Optional<CaseUpdateDTO> getCaseById(String caseId) throws CaseNotFoundException {

    Optional<CaseUpdateDTO> cazeOpt = caseRepository.readCaseUpdate(caseId);

    if (cazeOpt.isPresent()) {
      return cazeOpt;
    } else {
      throw new CaseNotFoundException("Case Not found: " + caseId);
    }
  }

  @Retryable(
      value = {UacNotFoundException.class},
      maxAttempts = 5,
      backoff = @Backoff(delay = 1000))
  public Optional<UacUpdateDTO> getUacByHash(String hash) throws UacNotFoundException {

    Optional<UacUpdateDTO> uacUpdateOpt = uacRepository.readUAC(hash);

    if (uacUpdateOpt.isPresent()) {
      return uacUpdateOpt;
    } else {
      throw new UacNotFoundException("Uac Not found: " + hash);
    }
  }
}
