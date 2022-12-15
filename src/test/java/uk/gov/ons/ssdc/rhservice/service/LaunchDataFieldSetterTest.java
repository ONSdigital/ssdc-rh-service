package uk.gov.ons.ssdc.rhservice.service;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.CollectionExerciseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.CollectionInstrumentSelectionRule;
import uk.gov.ons.ssdc.rhservice.model.dto.EqLaunchSettings;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.repository.CaseRepository;
import uk.gov.ons.ssdc.rhservice.model.repository.CollectionExerciseRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LaunchDataFieldSetterTest {
    private static final String TEST_URL = "testUrl";

    @Mock
    CollectionExerciseRepository collectionExerciseRepository;

    @Mock
    CaseRepository caseRepository;

    @InjectMocks
    LaunchDataFieldSetter launchDataFieldSetter;

    @Test
    public void testCollectionExerciseNotFound() {
        CollectionExerciseUpdateDTO collectionExerciseUpdateDTO =
                new CollectionExerciseUpdateDTO(
                        UUID.randomUUID().toString(),
                        List.of(new CollectionInstrumentSelectionRule(TEST_URL, null)));
        collectionExerciseRepository.writeCollectionExerciseUpdate(collectionExerciseUpdateDTO);
        when(collectionExerciseRepository.readCollectionExerciseUpdate(any())).thenReturn(Optional.of(collectionExerciseUpdateDTO));

        UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
        uacUpdateDTO.setCollectionInstrumentUrl(TEST_URL);
        uacUpdateDTO.setCollectionExerciseId(collectionExerciseUpdateDTO.getCollectionExerciseId());
        launchDataFieldSetter.stampLaunchDataFieldsOnUAC(uacUpdateDTO);

        assertThat(uacUpdateDTO.getLaunchData()).isNull();
    }


    @Test
    public void testNoLaunchSettings() {
        CollectionExerciseUpdateDTO collectionExerciseUpdateDTO =
                new CollectionExerciseUpdateDTO(
                        UUID.randomUUID().toString(),
                        List.of(new CollectionInstrumentSelectionRule(TEST_URL, null)));
        collectionExerciseRepository.writeCollectionExerciseUpdate(collectionExerciseUpdateDTO);
        when(collectionExerciseRepository.readCollectionExerciseUpdate(any())).thenReturn(Optional.of(collectionExerciseUpdateDTO));

        UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
        uacUpdateDTO.setCollectionInstrumentUrl(TEST_URL);
        uacUpdateDTO.setCollectionExerciseId(collectionExerciseUpdateDTO.getCollectionExerciseId());
        launchDataFieldSetter.stampLaunchDataFieldsOnUAC(uacUpdateDTO);

        assertThat(uacUpdateDTO.getLaunchData()).isNull();
    }

    @Test
    public void testLaunchSettingsEmpty() {
        CollectionExerciseUpdateDTO collectionExerciseUpdateDTO =
                new CollectionExerciseUpdateDTO(
                        UUID.randomUUID().toString(),
                        List.of(new CollectionInstrumentSelectionRule(TEST_URL, new ArrayList<>())));
        collectionExerciseRepository.writeCollectionExerciseUpdate(collectionExerciseUpdateDTO);
        when(collectionExerciseRepository.readCollectionExerciseUpdate(any())).thenReturn(Optional.of(collectionExerciseUpdateDTO));

        UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
        uacUpdateDTO.setCollectionInstrumentUrl(TEST_URL);
        uacUpdateDTO.setCollectionExerciseId(collectionExerciseUpdateDTO.getCollectionExerciseId());
        launchDataFieldSetter.stampLaunchDataFieldsOnUAC(uacUpdateDTO);

        assertThat(uacUpdateDTO.getLaunchData()).isNull();
    }
}