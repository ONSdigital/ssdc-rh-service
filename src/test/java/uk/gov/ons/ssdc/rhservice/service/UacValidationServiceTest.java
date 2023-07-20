package uk.gov.ons.ssdc.rhservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.CollectionExerciseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.UacOr4xxResponseEntity;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.repository.CaseRepository;
import uk.gov.ons.ssdc.rhservice.model.repository.CollectionExerciseRepository;
import uk.gov.ons.ssdc.rhservice.model.repository.UacRepository;

@ExtendWith(MockitoExtension.class)
@ExtendWith(OutputCaptureExtension.class)
class UacValidationServiceTest {
  public static final String UAC_HASH = "UAC_HASH";

  @Mock UacRepository uacRepository;

  @Mock CaseRepository caseRepository;

  @Mock CollectionExerciseRepository collectionExerciseRepository;

  @InjectMocks UacValidationService underTest;

  @Test
  public void testUacReturned() {
    // Given
    UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
    uacUpdateDTO.setReceiptReceived(false);
    uacUpdateDTO.setActive(true);
    uacUpdateDTO.setCaseId("Test");
    when(uacRepository.readUAC(any())).thenReturn(Optional.of(uacUpdateDTO));

    CaseUpdateDTO caseUpdateDTO = new CaseUpdateDTO();
    caseUpdateDTO.setCollectionExerciseId("Test");

    CollectionExerciseUpdateDTO collectionExerciseUpdateDTO = new CollectionExerciseUpdateDTO();
    collectionExerciseUpdateDTO.setEndDate(
        Date.from(OffsetDateTime.now(ZoneOffset.UTC).toInstant()));

    when(caseRepository.readCaseUpdate(any())).thenReturn(Optional.of(caseUpdateDTO));
    when(collectionExerciseRepository.readCollectionExerciseUpdate(any()))
        .thenReturn(Optional.of(collectionExerciseUpdateDTO));

    // When
    UacOr4xxResponseEntity uacOr4xxResponseEntity = underTest.getUac(UAC_HASH);

    // Then
    assertThat(uacOr4xxResponseEntity.getUacUpdateDTO()).isEqualTo(uacUpdateDTO);
    verify(uacRepository).readUAC(UAC_HASH);
  }

  @Test
  public void testUacNotFound() {
    UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
    uacUpdateDTO.setReceiptReceived(false);
    uacUpdateDTO.setActive(true);
    uacUpdateDTO.setCaseId("Test");
    when(uacRepository.readUAC(any())).thenReturn(Optional.empty());

    Optional<ResponseEntity> responseEntity =
        Optional.of(new ResponseEntity<>("UAC_NOT_FOUND", HttpStatus.NOT_FOUND));

    UacOr4xxResponseEntity uacOr4xxResponseEntity = underTest.getUac(UAC_HASH);

    assertThat(uacOr4xxResponseEntity.getResponseEntityOptional()).isEqualTo(responseEntity);

    verify(uacRepository).readUAC(eq(UAC_HASH));
  }

  @Test
  public void testUacIsReceipted() {
    UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
    uacUpdateDTO.setReceiptReceived(true);
    uacUpdateDTO.setActive(true);
    uacUpdateDTO.setCaseId("Test");
    when(uacRepository.readUAC(any())).thenReturn(Optional.of(uacUpdateDTO));

    Optional<ResponseEntity> responseEntity =
        Optional.of(new ResponseEntity<>("UAC_RECEIPTED", HttpStatus.BAD_REQUEST));

    UacOr4xxResponseEntity uacOr4xxResponseEntity = underTest.getUac(UAC_HASH);

    assertThat(uacOr4xxResponseEntity.getResponseEntityOptional()).isEqualTo(responseEntity);

    verify(uacRepository).readUAC(eq(UAC_HASH));
  }

  @Test
  public void testUacIsInactive() {
    UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
    uacUpdateDTO.setReceiptReceived(false);
    uacUpdateDTO.setActive(false);
    uacUpdateDTO.setCaseId("Test");
    when(uacRepository.readUAC(any())).thenReturn(Optional.of(uacUpdateDTO));

    Optional<ResponseEntity> responseEntity =
        Optional.of(new ResponseEntity<>("UAC_INACTIVE", HttpStatus.BAD_REQUEST));

    UacOr4xxResponseEntity uacOr4xxResponseEntity = underTest.getUac(UAC_HASH);

    assertThat(uacOr4xxResponseEntity.getResponseEntityOptional()).isEqualTo(responseEntity);

    verify(uacRepository).readUAC(eq(UAC_HASH));
  }

  @Test
  void testUacHasNoCaseId() {
    // Given
    UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
    uacUpdateDTO.setReceiptReceived(false);
    uacUpdateDTO.setActive(true);
    uacUpdateDTO.setCaseId(null);

    when(uacRepository.readUAC(any())).thenReturn(Optional.of(uacUpdateDTO));

    // When, Then
    RuntimeException thrownException =
        assertThrows(RuntimeException.class, () -> underTest.getUac(UAC_HASH));

    assertThat(thrownException.getMessage()).isEqualTo("UAC has no caseId");
  }

  @Test
  void testCaseNotFound(CapturedOutput loggingOutput) {
    // Given
    UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
    uacUpdateDTO.setReceiptReceived(false);
    uacUpdateDTO.setActive(true);
    uacUpdateDTO.setCaseId("NotFound");

    when(uacRepository.readUAC(any())).thenReturn(Optional.of(uacUpdateDTO));
    when(caseRepository.readCaseUpdate(any())).thenReturn(Optional.empty());

    // When, Then
    RuntimeException thrownException =
        assertThrows(RuntimeException.class, () -> underTest.getUac(UAC_HASH));

    assertThat(loggingOutput).contains("caseId not found for UAC", uacUpdateDTO.getCaseId());

    assertThat(thrownException.getMessage()).isEqualTo("Case Not Found for UAC");
  }

  @Test
  void testCollectionExerciseNotFound(CapturedOutput loggingOutput) {
    // Given
    UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
    uacUpdateDTO.setReceiptReceived(false);
    uacUpdateDTO.setActive(true);
    uacUpdateDTO.setCaseId("Test");

    CaseUpdateDTO caseUpdateDTO = new CaseUpdateDTO();
    caseUpdateDTO.setCaseId("Test");
    caseUpdateDTO.setCollectionExerciseId("NotFound");

    when(uacRepository.readUAC(any())).thenReturn(Optional.of(uacUpdateDTO));
    when(caseRepository.readCaseUpdate(any())).thenReturn(Optional.of(caseUpdateDTO));
    when(collectionExerciseRepository.readCollectionExerciseUpdate(any()))
        .thenReturn(Optional.empty());

    // When, Then
    RuntimeException thrownException =
        assertThrows(RuntimeException.class, () -> underTest.getUac(UAC_HASH));

    assertThat(loggingOutput)
        .contains(
            "collectionExerciseId not found for caseId",
            caseUpdateDTO.getCaseId(),
            caseUpdateDTO.getCollectionExerciseId());

    assertThat(thrownException.getMessage()).isEqualTo("Collection exercise not found for case");
  }

  @Test
  void testCollectionExerciseResponseExpiresAtDateHasPassed(CapturedOutput loggingOutput) {
    // Given
    UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
    uacUpdateDTO.setReceiptReceived(false);
    uacUpdateDTO.setActive(true);
    uacUpdateDTO.setCaseId("Test");

    CaseUpdateDTO caseUpdateDTO = new CaseUpdateDTO();
    caseUpdateDTO.setCollectionExerciseId("Test");

    CollectionExerciseUpdateDTO collectionExerciseUpdateDTO = new CollectionExerciseUpdateDTO();
    Date fourWeeksAgo = Date.from(OffsetDateTime.now(ZoneOffset.UTC).minusWeeks(4).toInstant());
    collectionExerciseUpdateDTO.setEndDate(fourWeeksAgo);

    when(uacRepository.readUAC(any())).thenReturn(Optional.of(uacUpdateDTO));
    when(caseRepository.readCaseUpdate(any())).thenReturn(Optional.of(caseUpdateDTO));
    when(collectionExerciseRepository.readCollectionExerciseUpdate(any()))
        .thenReturn(Optional.of(collectionExerciseUpdateDTO));

    Optional<ResponseEntity> responseEntity =
        Optional.of(new ResponseEntity<>("UAC_INACTIVE", HttpStatus.BAD_REQUEST));

    // When
    UacOr4xxResponseEntity uacOr4xxResponseEntity = underTest.getUac(UAC_HASH);

    // Then
    assertThat(uacOr4xxResponseEntity.getResponseEntityOptional()).isEqualTo(responseEntity);
    verify(uacRepository).readUAC(UAC_HASH);
    assertThat(loggingOutput)
        .contains("Collection exercise response expiry end date has already passed for case");
  }
}
