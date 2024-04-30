package uk.gov.ons.ssdc.rhservice.service;

import static uk.gov.ons.ssdc.rhservice.utils.Constants.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.CollectionExerciseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.UacOr4xxResponseEntity;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.repository.CaseRepository;
import uk.gov.ons.ssdc.rhservice.model.repository.CollectionExerciseRepository;
import uk.gov.ons.ssdc.rhservice.model.repository.UacRepository;

@Service
public class UacValidationService {
  private final UacRepository uacRepository;
  private final CaseRepository caseRepository;
  private final CollectionExerciseRepository collectionExerciseRepository;

  private static final Logger log = LoggerFactory.getLogger(UacValidationService.class);

  public UacValidationService(
      UacRepository uacRepository,
      CaseRepository caseRepository,
      CollectionExerciseRepository collectionExerciseRepository) {
    this.uacRepository = uacRepository;
    this.caseRepository = caseRepository;
    this.collectionExerciseRepository = collectionExerciseRepository;
  }

  public UacOr4xxResponseEntity getUac(String uacHash) throws RuntimeException {
    Optional<UacUpdateDTO> uacOpt = uacRepository.readUAC(uacHash);
    UacOr4xxResponseEntity uacOr4xxResponseEntity = new UacOr4xxResponseEntity();

    if (uacOpt.isEmpty()) {
      uacOr4xxResponseEntity.setResponseEntityOptional(
          Optional.of(new ResponseEntity<>(UAC_NOT_FOUND, HttpStatus.NOT_FOUND)));
      return uacOr4xxResponseEntity;
    }

    UacUpdateDTO uacUpdateDTO = uacOpt.get();

    if (uacUpdateDTO.isReceiptReceived()) {
      uacOr4xxResponseEntity.setResponseEntityOptional(
          Optional.of(new ResponseEntity<>(UAC_RECEIPTED, HttpStatus.BAD_REQUEST)));
      return uacOr4xxResponseEntity;
    }

    if (!uacUpdateDTO.isActive()) {
      uacOr4xxResponseEntity.setResponseEntityOptional(
          Optional.of(new ResponseEntity<>(UAC_INACTIVE, HttpStatus.BAD_REQUEST)));
      return uacOr4xxResponseEntity;
    }

    uacOr4xxResponseEntity.setUacUpdateDTO(uacUpdateDTO);

    CaseUpdateDTO caseUpdateDTO = getCaseFromUac(uacUpdateDTO);
    uacOr4xxResponseEntity.setCaseUpdateDTO(caseUpdateDTO);

    CollectionExerciseUpdateDTO collectionExerciseUpdateDTO =
        getCollectionExerciseFromCase(caseUpdateDTO);

    if (collectionExerciseResponseExpiresAtDateHasPassed(
        collectionExerciseUpdateDTO, caseUpdateDTO)) {
      uacOr4xxResponseEntity.setResponseEntityOptional(
          Optional.of(new ResponseEntity<>(UAC_INACTIVE, HttpStatus.BAD_REQUEST)));
      return uacOr4xxResponseEntity;
    }

    uacOr4xxResponseEntity.setCollectionExerciseUpdateDTO(collectionExerciseUpdateDTO);

    uacOr4xxResponseEntity.setResponseEntityOptional(Optional.empty());
    return uacOr4xxResponseEntity;
  }

  private CollectionExerciseUpdateDTO getCollectionExerciseFromCase(CaseUpdateDTO caseUpdateDTO) {
    return collectionExerciseRepository
        .readCollectionExerciseUpdate(caseUpdateDTO.getCollectionExerciseId())
        .orElseThrow(
            () -> {
              log.atError()
                  .setMessage("collectionExerciseId not found for caseId")
                  .addKeyValue("collectionExerciseId", caseUpdateDTO.getCollectionExerciseId())
                  .addKeyValue("caseId", caseUpdateDTO.getCaseId())
                  .log();
              return new RuntimeException("Collection exercise not found for case");
            });
  }

  private boolean collectionExerciseResponseExpiresAtDateHasPassed(
      CollectionExerciseUpdateDTO collectionExerciseUpdateDTO, CaseUpdateDTO caseUpdateDTO) {
    OffsetDateTime collectionExerciseEndDate =
        collectionExerciseUpdateDTO.getEndDate().toInstant().atOffset(ZoneOffset.UTC);
    OffsetDateTime collectionExerciseEndDateWithWeekIncrement =
        collectionExerciseEndDate.plusWeeks(RESPONSE_EXPIRES_AT_WEEK_INCREMENT);

    if (collectionExerciseEndDateWithWeekIncrement.isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
      log.atWarn()
          .setMessage("Collection exercise response expiry end date has already passed for case")
          .addKeyValue(
              "collectionExerciseId", collectionExerciseUpdateDTO.getCollectionExerciseId())
          .addKeyValue("caseId", caseUpdateDTO.getCaseId())
          .addKeyValue("collectionExerciseEndDate", collectionExerciseEndDate.toString())
          .addKeyValue(
              "collectionExerciseWeeksInFutureIncrement", RESPONSE_EXPIRES_AT_WEEK_INCREMENT)
          .addKeyValue(
              "collectionExerciseEndDateWithWeekIncrement",
              collectionExerciseEndDateWithWeekIncrement.toString())
          .log();
      return true;
    }
    return false;
  }

  private CaseUpdateDTO getCaseFromUac(UacUpdateDTO uacUpdateDTO) {

    String caseId = uacUpdateDTO.getCaseId();
    if (StringUtils.isEmpty(caseId)) {
      throw new RuntimeException("UAC has no caseId");
    }

    return caseRepository
        .readCaseUpdate(caseId)
        .orElseThrow(
            () -> {
              log.atError()
                  .setMessage("caseId not found for UAC")
                  .addKeyValue("caseId", uacUpdateDTO.getCaseId())
                  .log();
              return new RuntimeException("Case Not Found for UAC");
            });
  }
}
