package uk.gov.ons.ssdc.rhservice.survey.specific;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.repository.CaseRepository;

@Component
public class LaunchDataFieldSetter {
  private final CaseRepository caseRepository;
  //  final ObjectMapper mapper = new ObjectMapper();
  // Rules for each UAC check it's survey, get the rule, apply them
  // One day rules may include overwrite etc

  public LaunchDataFieldSetter(CaseRepository caseRepository) {

    this.caseRepository = caseRepository;
  }

  public void stampLaunchDataFieldsOnUAC(UacUpdateDTO uacUpdateDTO) {
    Optional<CaseUpdateDTO> cazeOpt = caseRepository.readCaseUpdate(uacUpdateDTO.getCaseId());
    if (cazeOpt.isEmpty()) {
      throw new RuntimeException("Not Found case ID: " + uacUpdateDTO.getCaseId());
    }

    CaseUpdateDTO caze = cazeOpt.get();
    //    CollectionExerciseUpdateDTO collectionExerciseUpdateDTO =
    //        getCollex(caze.getCollectionExerciseId());

    if (!caze.getSample().containsKey("PARTICIPANT_ID")) {
      return;
    }

    Map<String, String> launchData = new HashMap<>();
    launchData.put("participant_id", caze.getSample().get("PARTICIPANT_ID"));
    launchData.put("first_name", caze.getSample().get("FIRST_NAME"));
    uacUpdateDTO.setLaunchData(launchData);

    // have proper Java classes to map this JSON too
    //        ArrayList<Object> collexRules = (ArrayList<Object>)
    // collectionExerciseUpdateDTO.getCollectionInstrumentRules();
    //        Map<String, Object> collexRule = (Map<String, Object>) collexRules.get(0);
    //
    //        //        TODO: nice way of if x do this etc?
    //        if (!collexRule.containsKey("eqLaunchDataSettings")) {
    //            return;
    //        }
    //
    //        List<Map<String, String>> launchDataSettingsMap =
    //                (List<Map<String, String>>) collexRule.get("eqLaunchDataSettings");
    //
    //        Map<String, String> launchData = new HashMap<>();
    //
    //        for (Map<String, String> launchDataField : launchDataSettingsMap) {
    //            final LaunchDataFieldDTO launchDataFieldDTO =
    //                    mapper.convertValue(launchDataField, LaunchDataFieldDTO.class);
    //
    //            if (caze.getSample().containsKey(launchDataFieldDTO.getSampleField())) {
    //                launchData.put(
    //                        launchDataFieldDTO.getLaunchDataFieldName(),
    //                        caze.getSample().get(launchDataFieldDTO.getSampleField()));
    //            } else if (launchDataFieldDTO.isMandatory()) {
    //                throw new RuntimeException(
    //                        "Expected field: "
    //                                + launchDataFieldDTO.getSampleField()
    //                                + " missing on case id: "
    //                                + caze.getCaseId());
    //            }
    //        }

  }

  //  private CollectionExerciseUpdateDTO getCollex(String collexId) {
  //
  //    Optional<CollectionExerciseUpdateDTO> collex =
  //        collectionExerciseRepository.readCollectionExerciseUpdate(collexId);
  //    if (collex.isEmpty()) {
  //      throw new RuntimeException("Not Found Collection Exercise: " + collexId);
  //    }
  //
  //    return collex.get();
  //  }
}
