package uk.gov.ons.ssdc.rhservice.utils;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.ons.ssdc.rhservice.exceptions.CTPException;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.repository.CaseRepository;

@Component
@ActiveProfiles("test")
public class FireStorePoller {
  @Autowired private CaseRepository caseRepository;

  @Retryable(
      value = {CaseNotFoundException.class},
      maxAttempts = 5,
      backoff = @Backoff(delay = 1000))
  public Optional<CaseUpdateDTO> getCaseById(String caseId)
      throws CaseNotFoundException, CTPException {

    Optional<CaseUpdateDTO> cazeOpt = caseRepository.readCaseUpdate(caseId);

    if (cazeOpt.isPresent()) {
      return cazeOpt;
    } else {
      throw new CaseNotFoundException("Case Not found: " + caseId);
    }
  }
}
