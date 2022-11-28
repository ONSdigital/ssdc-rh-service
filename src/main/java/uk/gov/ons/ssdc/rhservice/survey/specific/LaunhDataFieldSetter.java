package uk.gov.ons.ssdc.rhservice.survey.specific;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.CollectionExerciseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.LaunchDataFieldDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.SurveyUpdateDto;
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
    SurveyUpdateDto survey = getSurveyFromCollexId(caze.getCollectionExerciseId());

    if (survey.getMetadata() == null) {
      return;
    }

    if (!survey.getMetadata().containsKey("launchDataSettings")) {
      return;
    }

    final ObjectMapper mapper = new ObjectMapper();
    List<Map<String, String>> launchDataSettingsMap =
        (List<Map<String, String>>) survey.getMetadata().get("launchDataSettings");

    Map<String, String> launchData = new HashMap<>();

    for (Map<String, String> launchDataField : launchDataSettingsMap) {
      final LaunchDataFieldDTO launchDataFieldDTO =
          mapper.convertValue(launchDataField, LaunchDataFieldDTO.class);

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

  private SurveyUpdateDto getSurveyFromCollexId(String collexId) {

    Optional<CollectionExerciseUpdateDTO> collex =
        collectionExerciseRepository.readCollectionExerciseUpdate(collexId);
    if (collex.isEmpty()) {
      throw new RuntimeException("Not Found Collection Exercise: " + collexId);
    }

    Optional<SurveyUpdateDto> survey =
        surveyRepository.readSurveyUpdate(collex.get().getSurveyId());
    if (survey.isEmpty()) {
      throw new RuntimeException("Not Found Survey: " + collex.get().getSurveyId());
    }

    return survey.get();
  }
}
