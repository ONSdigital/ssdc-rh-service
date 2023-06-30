package uk.gov.ons.ssdc.rhservice.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.ons.ssdc.rhservice.model.dto.CollectionExerciseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.CollectionInstrumentSelectionRule;
import uk.gov.ons.ssdc.rhservice.model.dto.EqLaunchSettings;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.repository.CaseRepository;
import uk.gov.ons.ssdc.rhservice.model.repository.CollectionExerciseRepository;

@ExtendWith(MockitoExtension.class)
class LaunchDataFieldSetterTest {
  private static final String TEST_URL = "testUrl";

  @Mock CollectionExerciseRepository collectionExerciseRepository;

  @Mock CaseRepository caseRepository;

  @InjectMocks LaunchDataFieldSetter underTest;

  @Test
  public void testCollectionExerciseNotFound() {
    when(collectionExerciseRepository.readCollectionExerciseUpdate(any()))
        .thenReturn(Optional.empty());

    UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
    uacUpdateDTO.setCollectionInstrumentUrl(TEST_URL);
    uacUpdateDTO.setCollectionExerciseId(UUID.randomUUID().toString());

    RuntimeException thrown =
        assertThrows(
            RuntimeException.class, () -> underTest.stampLaunchDataFieldsOnUAC(uacUpdateDTO));

    Assertions.assertThat(thrown.getMessage())
        .isEqualTo("Collection Exercise not found: " + uacUpdateDTO.getCollectionExerciseId());
  }

  @Test
  public void testCollectionInstrumentUrlNotFound() {
    CollectionExerciseUpdateDTO collectionExerciseUpdateDTO =
        new CollectionExerciseUpdateDTO(
            UUID.randomUUID().toString(),
            List.of(new CollectionInstrumentSelectionRule(TEST_URL, null)),
            "collex1",
            UUID.randomUUID().toString(),
            "clx",
            Date.from(Instant.now()),
            Date.from(Instant.now()),
            List.of());
    collectionExerciseRepository.writeCollectionExerciseUpdate(collectionExerciseUpdateDTO);
    when(collectionExerciseRepository.readCollectionExerciseUpdate(any()))
        .thenReturn(Optional.of(collectionExerciseUpdateDTO));

    UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
    uacUpdateDTO.setCollectionInstrumentUrl("unMatchedUrl");
    uacUpdateDTO.setCollectionExerciseId(UUID.randomUUID().toString());

    RuntimeException thrown =
        assertThrows(
            RuntimeException.class, () -> underTest.stampLaunchDataFieldsOnUAC(uacUpdateDTO));

    Assertions.assertThat(thrown.getMessage())
        .isEqualTo("Collection Instrument Url not matched: unMatchedUrl");
  }

  @Test
  public void testCaseNotFound() {
    CollectionExerciseUpdateDTO collectionExerciseUpdateDTO =
        new CollectionExerciseUpdateDTO(
            UUID.randomUUID().toString(),
            List.of(
                new CollectionInstrumentSelectionRule(
                    TEST_URL, List.of(new EqLaunchSettings("a", "b", true)))),
            "collex1",
            UUID.randomUUID().toString(),
            "clx",
            Date.from(Instant.now()),
            Date.from(Instant.now()),
            List.of());

    collectionExerciseRepository.writeCollectionExerciseUpdate(collectionExerciseUpdateDTO);
    when(collectionExerciseRepository.readCollectionExerciseUpdate(any()))
        .thenReturn(Optional.of(collectionExerciseUpdateDTO));
    when(caseRepository.readCaseUpdate(any())).thenReturn(Optional.empty());

    UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
    uacUpdateDTO.setCollectionInstrumentUrl(TEST_URL);
    uacUpdateDTO.setCollectionExerciseId(UUID.randomUUID().toString());
    uacUpdateDTO.setCaseId(UUID.randomUUID().toString());

    RuntimeException thrown =
        assertThrows(
            RuntimeException.class, () -> underTest.stampLaunchDataFieldsOnUAC(uacUpdateDTO));

    Assertions.assertThat(thrown.getMessage())
        .isEqualTo("Not Found case ID: " + uacUpdateDTO.getCaseId());
  }

  @Test
  public void testNoLaunchSettings() {
    CollectionExerciseUpdateDTO collectionExerciseUpdateDTO =
        new CollectionExerciseUpdateDTO(
            UUID.randomUUID().toString(),
            List.of(new CollectionInstrumentSelectionRule(TEST_URL, null)),
            "collex1",
            UUID.randomUUID().toString(),
            "clx",
            Date.from(Instant.now()),
            Date.from(Instant.now()),
            List.of());
    collectionExerciseRepository.writeCollectionExerciseUpdate(collectionExerciseUpdateDTO);
    when(collectionExerciseRepository.readCollectionExerciseUpdate(any()))
        .thenReturn(Optional.of(collectionExerciseUpdateDTO));

    UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
    uacUpdateDTO.setCollectionInstrumentUrl(TEST_URL);
    uacUpdateDTO.setCollectionExerciseId(collectionExerciseUpdateDTO.getCollectionExerciseId());
    underTest.stampLaunchDataFieldsOnUAC(uacUpdateDTO);

    assertThat(uacUpdateDTO.getLaunchData()).isNull();
  }

  @Test
  public void testLaunchSettingsEmpty() {
    CollectionExerciseUpdateDTO collectionExerciseUpdateDTO =
        new CollectionExerciseUpdateDTO(
            UUID.randomUUID().toString(),
            List.of(new CollectionInstrumentSelectionRule(TEST_URL, new ArrayList<>())),
            "collex1",
            UUID.randomUUID().toString(),
            "clx",
            Date.from(Instant.now()),
            Date.from(Instant.now()),
            List.of());
    collectionExerciseRepository.writeCollectionExerciseUpdate(collectionExerciseUpdateDTO);
    when(collectionExerciseRepository.readCollectionExerciseUpdate(any()))
        .thenReturn(Optional.of(collectionExerciseUpdateDTO));

    UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
    uacUpdateDTO.setCollectionInstrumentUrl(TEST_URL);
    uacUpdateDTO.setCollectionExerciseId(collectionExerciseUpdateDTO.getCollectionExerciseId());
    underTest.stampLaunchDataFieldsOnUAC(uacUpdateDTO);

    assertThat(uacUpdateDTO.getLaunchData()).isNull();
  }
}
