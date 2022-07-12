package uk.gov.ons.ssdc.rhservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.UacOr4xxResponseEntity;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.repository.CaseRepository;
import uk.gov.ons.ssdc.rhservice.model.repository.UacRepository;

@ExtendWith(MockitoExtension.class)
class UacServiceTest {
  public static final String CASE_ID = "CASE_ID";
  public static final String UAC_HASH = "UAC_HASH";

  @Mock UacRepository uacRepository;

  @Mock CaseRepository caseRepository;

  @InjectMocks UacService underTest;

  @Test
  public void testUacReturned() {
    UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
    uacUpdateDTO.setReceiptReceived(false);
    uacUpdateDTO.setActive(true);
    uacUpdateDTO.setCaseId("Test");
    when(uacRepository.readUAC(any())).thenReturn(Optional.of(uacUpdateDTO));

    CaseUpdateDTO caseUpdateDTO = new CaseUpdateDTO();
    when(caseRepository.readCaseUpdate(any())).thenReturn(Optional.of(caseUpdateDTO));

    UacOr4xxResponseEntity uacOr4xxResponseEntity = underTest.getUac(UAC_HASH);

    assertThat(uacOr4xxResponseEntity.getUacUpdateDTO()).isEqualTo(uacUpdateDTO);

    verify(uacRepository).readUAC(eq(UAC_HASH));
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
}
