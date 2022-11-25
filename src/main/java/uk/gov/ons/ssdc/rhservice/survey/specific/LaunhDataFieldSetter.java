package uk.gov.ons.ssdc.rhservice.survey.specific;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.CollectionExerciseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.LaunchDataFieldDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.SurveyDto;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.repository.CaseRepository;
import uk.gov.ons.ssdc.rhservice.model.repository.CollectionExerciseRepository;
import uk.gov.ons.ssdc.rhservice.model.repository.SurveyRepository;

@Component
public class LaunhDataFieldSetter {
  private final SurveyRepository surveyRepository;
  private final CollectionExerciseRepository collectionExerciseRepository;
  private final CaseRepository caseRepository;
  // Rules for each UAC check it's survey, get the rule, apply them
  // One day rules may include overwrite etc

  public LaunhDataFieldSetter(
      SurveyRepository surveyRepository,
      CollectionExerciseRepository collectionExerciseRepository,
      CaseRepository caseRepository) {

    this.surveyRepository = surveyRepository;
    this.collectionExerciseRepository = collectionExerciseRepository;
    this.caseRepository = caseRepository;
  }

  public void stampLaunchDataFieldsOnUAC(UacUpdateDTO uacUpdateDTO) {
    Optional<CaseUpdateDTO> cazeOpt = caseRepository.readCaseUpdate(uacUpdateDTO.getCaseId());
    if (cazeOpt.isEmpty()) {
      throw new RuntimeException("Not Found case ID: " + uacUpdateDTO.getCaseId());
    }

    CaseUpdateDTO caze = cazeOpt.get();
    SurveyDto survey = getSurveyFromCollexId(caze.getCollectionExerciseId());

    if (survey.getMetadata() == null) {
      return;
    }

    if (!survey.getMetadata().containsKey("launchDataSettings")) {
      return;
    }

    Map<String, LaunchDataFieldDTO> launchDataSettings =
        (Map<String, LaunchDataFieldDTO>) survey.getMetadata().get("launchDataSettings");
    Map<String, String> launchData = new HashMap<>();

    for (Map.Entry<String, LaunchDataFieldDTO> entry : launchDataSettings.entrySet()) {
      String sampleKey = entry.getKey();
      LaunchDataFieldDTO launchDataFieldDTO = entry.getValue();

      if (caze.getSample().containsKey(sampleKey)) {
        launchData.put(
            launchDataFieldDTO.getLaunchDataFieldName(), caze.getSample().get(sampleKey));
      } else {
        if (launchDataFieldDTO.isMandatory()) {
          throw new RuntimeException(
              "Expected field: " + sampleKey + " missing on case id: " + caze.getCaseId());
        }
      }
    }

    uacUpdateDTO.setLaunchData(launchData);
  }

  private SurveyDto getSurveyFromCollexId(String collexId) {

    Optional<CollectionExerciseUpdateDTO> collex =
        collectionExerciseRepository.readCollectionExerciseUpdate(collexId);
    if (collex.isEmpty()) {
      throw new RuntimeException("Not Found Collection Exercise: " + collexId);
    }

    Optional<SurveyDto> survey = surveyRepository.readSurveyUpdate(collex.get().getSurveyId());
    if (survey.isEmpty()) {
      throw new RuntimeException("Not Found Survey: " + collex.get().getSurveyId());
    }

    return survey.get();
  }
}
