package uk.gov.ons.ssdc.rhservice.survey.specific;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.repository.CaseRepository;
import uk.gov.ons.ssdc.rhservice.model.repository.UacRepository;

@Component
public class PhmSpecific {
  private final CaseRepository caseRepository;
  private final UacRepository uacRepository;

  // This approach relies on sample field and desired field matching, otherwise a more complex map
  // required with fromField, toField
  public static final String[] phmLaunchFieldsFromSample =
      new String[] {
        "SWAB_BARCODE", "BLOOD_BARCODE", "PARTICIPANT_ID", "LONGITUDINAL_QUESTIONS", "FIRST_NAME"
      };

  public PhmSpecific(CaseRepository caseRepository, UacRepository uacRepository) {
    this.caseRepository = caseRepository;
    this.uacRepository = uacRepository;
  }

  public Map<String, String> getPHMFieldsToStampFromCase(UacUpdateDTO uacUpdateDTO) {
    Optional<UacUpdateDTO> uacUpdate = uacRepository.readUAC(uacUpdateDTO.getUacHash());

    if (uacUpdate.isPresent()) {
      // this is called if the UACUpdateDTO is active and existing in FireStore.
      // we don't want any new case updates overwriting the launch values
      return uacUpdate.get().getLaunchData();
    }

    CaseUpdateDTO caze = checkCheckExists(uacUpdateDTO.getCaseId());

    Map<String, String> phmLaunchData = new HashMap<>();

    for (String fieldName : phmLaunchFieldsFromSample) {
      // This presumes that the required data will always be there.
      // We can check for this, but the whole data is corrupt if it's not there?
      phmLaunchData.put(fieldName, caze.getSample().get(fieldName));
    }

    return phmLaunchData;
  }

  private CaseUpdateDTO checkCheckExists(String caseId) {

    Optional<CaseUpdateDTO> cazeOpt = caseRepository.readCaseUpdate(caseId);

    if (cazeOpt.isEmpty()) {
      // For PHM, we create all UACs after cases - this will force a retry?
      // Is there any reason for an exception manager here?
      throw new RuntimeException("Failed to find case id " + caseId);
    }

    return cazeOpt.get();
  }
}
