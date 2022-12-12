package uk.gov.ons.ssdc.rhservice.survey.specific;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.LaunchDataFieldDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.repository.CaseRepository;
import uk.gov.ons.ssdc.rhservice.model.repository.CollectionExerciseRepository;

@Component
public class LaunchDataFieldSetter {
    private final CaseRepository caseRepository;
    private final CollectionExerciseRepository collectionExerciseRepository;

    public LaunchDataFieldSetter(
            CaseRepository caseRepository, CollectionExerciseRepository collectionExerciseRepository) {
        this.caseRepository = caseRepository;
        this.collectionExerciseRepository = collectionExerciseRepository;
    }

    public void stampLaunchDataFieldsOnUAC(UacUpdateDTO uacUpdateDTO) {
        List<LaunchDataFieldDTO> eqLaunchDataSettings =
                getEqLaunchSettingsFromCollectionExercise(
                        uacUpdateDTO.getCollectionExerciseId(), uacUpdateDTO.getCollectionInstrumentUrl());

        if (eqLaunchDataSettings == null || eqLaunchDataSettings.isEmpty()) {
            return;
        }

        CaseUpdateDTO caze = getCase(uacUpdateDTO.getCaseId());

        Map<String, String> launchData = new HashMap<>();
        for (LaunchDataFieldDTO launchDataFieldDTO : eqLaunchDataSettings) {

            if (caze.getSample().containsKey(launchDataFieldDTO.getSampleField())) {
                launchData.put(
                        launchDataFieldDTO.getLaunchDataFieldName(),
                        caze.getSample().get(launchDataFieldDTO.getSampleField()));
            } else if (launchDataFieldDTO.isMandatory()) {
                throw new RuntimeException(
                        "Expected field: "
                                + launchDataFieldDTO.getSampleField()
                                + " missing on case id: "
                                + caze.getCaseId());
            }
        }

        uacUpdateDTO.setLaunchData(launchData);
    }

    private List<LaunchDataFieldDTO> getEqLaunchSettingsFromCollectionExercise(
            String collectionExerciseId, String collectionInstrumentUrl) {
        return collectionExerciseRepository.readCollectionExerciseUpdate(collectionExerciseId)
                .orElseThrow(
                        () -> new RuntimeException("Collection Exercise not found: " + collectionExerciseId))
                .getCollectionInstrumentRules().stream()
                .filter(
                        collexInstrumentRule ->
                                collexInstrumentRule.getCollectionInstrumentUrl().equals(collectionInstrumentUrl))
                .findFirst()
                .orElseThrow(
                        () ->
                                new RuntimeException(
                                        "Collection Instrument Url not matched: " + collectionInstrumentUrl))
                .getEqLaunchSettings();
    }

    private CaseUpdateDTO getCase(String caseId) {
        return caseRepository
                .readCaseUpdate(caseId)
                .orElseThrow(() -> new RuntimeException("Not Found case ID: " + caseId));
    }
}
