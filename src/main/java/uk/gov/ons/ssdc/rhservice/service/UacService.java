package uk.gov.ons.ssdc.rhservice.service;

import static uk.gov.ons.ssdc.rhservice.utils.Constants.RESPONSE_EXPIRES_AT_WEEK_INCREMENT;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
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
public class UacService {
  private final UacRepository uacRepository;
  private final CaseRepository caseRepository;
  private final CollectionExerciseRepository collectionExerciseRepository;

  private static final Logger log = LoggerFactory.getLogger(UacService.class);

  public UacService(
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
          Optional.of(new ResponseEntity<>("UAC_NOT_FOUND", HttpStatus.NOT_FOUND)));
      return uacOr4xxResponseEntity;
    }

    UacUpdateDTO uacUpdateDTO = uacOpt.get();

    if (uacUpdateDTO.isReceiptReceived()) {
      uacOr4xxResponseEntity.setResponseEntityOptional(
          Optional.of(new ResponseEntity<>("UAC_RECEIPTED", HttpStatus.BAD_REQUEST)));
      return uacOr4xxResponseEntity;
    }

    if (!uacUpdateDTO.isActive()) {
      uacOr4xxResponseEntity.setResponseEntityOptional(
          Optional.of(new ResponseEntity<>("UAC_INACTIVE", HttpStatus.BAD_REQUEST)));
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
          Optional.of(new ResponseEntity<>("UAC_INVALID", HttpStatus.BAD_REQUEST)));
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
            () ->
                new RuntimeException(
                    String.format(
                        "collectionExerciseId '%s' not found for caseId '%s'",
                        caseUpdateDTO.getCollectionExerciseId(), caseUpdateDTO.getCaseId())));
  }

  private boolean collectionExerciseResponseExpiresAtDateHasPassed(
      CollectionExerciseUpdateDTO collectionExerciseUpdateDTO, CaseUpdateDTO caseUpdateDTO) {
    OffsetDateTime collectionExerciseResponseExpiresAtDate =
        collectionExerciseUpdateDTO
            .getEndDate()
            .toInstant()
            .atOffset(ZoneOffset.UTC)
            .plusWeeks(RESPONSE_EXPIRES_AT_WEEK_INCREMENT);

    if (collectionExerciseResponseExpiresAtDate.isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
      log.with("collectionExerciseId", collectionExerciseUpdateDTO.getCollectionExerciseId())
          .with("caseId", caseUpdateDTO.getCaseId())
          .with("collectionExerciseEndDate", collectionExerciseUpdateDTO.getEndDate())
          .with("collectionExerciseWeeksInFutureOffset", RESPONSE_EXPIRES_AT_WEEK_INCREMENT)
          .with("collectionExerciseResponseExpiresAtDate", collectionExerciseResponseExpiresAtDate)
          .warn("Collection exercise response expiry end date has already passed for case");
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
        .orElseThrow(() -> new RuntimeException("Case Not Found for UAC"));
  }
}
