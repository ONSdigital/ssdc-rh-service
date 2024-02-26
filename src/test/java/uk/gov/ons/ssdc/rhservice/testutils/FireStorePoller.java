package uk.gov.ons.ssdc.rhservice.testutils;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.ons.ssdc.rhservice.exceptions.CaseNotFoundException;
import uk.gov.ons.ssdc.rhservice.exceptions.CollectionExerciseNotFoundException;
import uk.gov.ons.ssdc.rhservice.exceptions.UacNotFoundException;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.CollectionExerciseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.repository.CaseRepository;
import uk.gov.ons.ssdc.rhservice.model.repository.CollectionExerciseRepository;
import uk.gov.ons.ssdc.rhservice.model.repository.UacRepository;

@Component
@ActiveProfiles("test")
public class FireStorePoller {
  @Autowired private CaseRepository caseRepository;
  @Autowired private UacRepository uacRepository;
  @Autowired private CollectionExerciseRepository collectionExerciseRepository;

  @Retryable(
      value = {CaseNotFoundException.class},
      maxAttempts = 5,
      backoff = @Backoff(delay = 1000),
      listeners = {"retryListener"})
  public Optional<CaseUpdateDTO> getCaseById(String caseId) throws CaseNotFoundException {

    Optional<CaseUpdateDTO> cazeOpt = caseRepository.readCaseUpdate(caseId);

    if (cazeOpt.isPresent()) {
      return cazeOpt;
    }

    throw new CaseNotFoundException("Case Not found: " + caseId);
  }

  @Retryable(
      value = {UacNotFoundException.class},
      maxAttempts = 5,
      backoff = @Backoff(delay = 1000),
      listeners = {"retryListener"})
  public Optional<UacUpdateDTO> getUacByHash(String hash, boolean expectedActive)
      throws UacNotFoundException {
    Optional<UacUpdateDTO> uacUpdateOpt = uacRepository.readUAC(hash);

    if (uacUpdateOpt.isPresent()) {
      if (uacUpdateOpt.get().isActive() != expectedActive) {
        throw new UacNotFoundException(
            "UAC found but not in right active state, expected: " + expectedActive);
      }

      return uacUpdateOpt;
    }

    throw new UacNotFoundException("Uac Not found: " + hash);
  }

  @Retryable(
      value = {UacNotFoundException.class},
      maxAttempts = 5,
      backoff = @Backoff(delay = 1000),
      listeners = {"retryListener"})
  public Optional<UacUpdateDTO> getUacByHashUacActiveValue(String hash, boolean expectedActiveValue)
      throws UacNotFoundException {

    Optional<UacUpdateDTO> uacUpdateOpt = uacRepository.readUAC(hash);

    if (uacUpdateOpt.isPresent() && uacUpdateOpt.get().isActive() == expectedActiveValue) {
      return uacUpdateOpt;
    }

    throw new UacNotFoundException("Updated Uac Not found: " + hash);
  }

  @Retryable(
      value = {UacNotFoundException.class},
      maxAttempts = 5,
      backoff = @Backoff(delay = 1000))
  public Optional<UacUpdateDTO> getUacByHashUacCheckSample(String hash, boolean expectedActiveValue)
      throws UacNotFoundException {

    Optional<UacUpdateDTO> uacUpdateOpt = uacRepository.readUAC(hash);

    if (uacUpdateOpt.isPresent() && uacUpdateOpt.get().isActive() == expectedActiveValue) {
      return uacUpdateOpt;
    }

    throw new UacNotFoundException("Updated Uac Not found: " + hash);
  }

  @Retryable(
      value = {UacNotFoundException.class},
      maxAttempts = 5,
      backoff = @Backoff(delay = 1000),
      listeners = {"retryListener"})
  public Optional<UacUpdateDTO> getUACByHashAndQID(String hash, String expectedQID)
      throws UacNotFoundException {

    Optional<UacUpdateDTO> uacUpdateOpt = uacRepository.readUAC(hash);

    if (uacUpdateOpt.isPresent() && uacUpdateOpt.get().getQid().equals(expectedQID)) {
      return uacUpdateOpt;
    }

    throw new UacNotFoundException("Updated Uac Not found: " + hash);
  }

  @Retryable(
      value = {CollectionExerciseNotFoundException.class},
      maxAttempts = 5,
      backoff = @Backoff(delay = 1000),
      listeners = {"retryListener"})
  public Optional<CollectionExerciseUpdateDTO> getCollectionExerciseById(
      String collectionExcerciseId) throws CollectionExerciseNotFoundException {

    Optional<CollectionExerciseUpdateDTO> collexOpt =
        collectionExerciseRepository.readCollectionExerciseUpdate(collectionExcerciseId);

    if (collexOpt.isPresent()) {
      return collexOpt;
    } else {
      throw new CollectionExerciseNotFoundException(
          "Collection Exercise Not found: " + collectionExcerciseId);
    }
  }
}
