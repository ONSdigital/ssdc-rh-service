package uk.gov.ons.ssdc.rhservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
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
  public void testUacHashMatched() {
    UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
    when(uacRepository.readUAC(any())).thenReturn(Optional.of(uacUpdateDTO));

    Optional<UacUpdateDTO> uacOpt = underTest.getUac(UAC_HASH);
    assertThat(uacOpt.get()).isEqualTo(uacUpdateDTO);

    verify(uacRepository).readUAC(eq(UAC_HASH));
  }

  @Test
  public void checkCaseReturned() {
    UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
    uacUpdateDTO.setCaseId(CASE_ID);

    CaseUpdateDTO caseUpdateDTO = new CaseUpdateDTO();
    when(caseRepository.readCaseUpdate(any())).thenReturn(Optional.of(caseUpdateDTO));

    assertThat(underTest.getCaseFromUac(uacUpdateDTO)).isEqualTo(caseUpdateDTO);

    verify(caseRepository).readCaseUpdate(eq(CASE_ID));
  }

  @Test
  public void uacHashNotFound() {
    when(uacRepository.readUAC(any())).thenReturn(Optional.empty());

    assertThat(underTest.getUac(UAC_HASH)).isEmpty();
  }

  @Test
  public void uacHasNoCaseId() {
    UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();

    RuntimeException thrown =
        assertThrows(RuntimeException.class, () -> underTest.getCaseFromUac(uacUpdateDTO));

    assertThat(thrown.getMessage()).isEqualTo("UAC has no caseId");
  }

  @Test
  public void caseNotFound() {
    UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
    uacUpdateDTO.setCaseId(CASE_ID);

    when(caseRepository.readCaseUpdate(any())).thenReturn(Optional.empty());

    RuntimeException thrown =
        assertThrows(RuntimeException.class, () -> underTest.getCaseFromUac(uacUpdateDTO));

    assertThat(thrown.getMessage()).isEqualTo("Case Not Found for UAC");

    verify(caseRepository).readCaseUpdate(eq(CASE_ID));
  }
}
