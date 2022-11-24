package uk.gov.ons.ssdc.rhservice.survey.specific;

import org.springframework.stereotype.Component;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.repository.CaseRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class PhmSpecific {

    private final CaseRepository caseRepository;

    public PHMspecific(CaseRepository caseRepository) {
        this.caseRepository = caseRepository;
    }

    public Map<String, String> getPHMFieldsToStampFromCase(UacUpdateDTO uacUpdateDTO) {
        Optional<CaseUpdateDTO> cazeOpt = caseRepository.readCaseUpdate(uacUpdateDTO.getCaseId());

        if(cazeOpt.isEmpty()) {
            // For PHM, we create all UACs after cases - this will force a retry?
            // Is there any reason for an exception manager here?
            throw new RuntimeException("Failed to find case id " + uacUpdateDTO.getCaseId());
        }

        CaseUpdateDTO caze = cazeOpt.get();

        //This approach relies on sample field and desired field matching, otherwise a more complex map required with fromField, toField
        String[] phmLaunchFieldsFromSample = new String[]{"SWAB_BARCODE", "BLOOD_BARCODE", "PARTICIPANT_ID", "LONGITUDINAL_QUESTIONS", "FIRST_NAME"};

        Map<String,String> phmLaunchData = new HashMap<>();

        for(String fieldName: phmLaunchFieldsFromSample) {
            // This presumes that the required data will always be there.
            // We can check for this, but
            phmLaunchData.put(fieldName, caze.getSample().get(fieldName));
        }

        return phmLaunchData;
    }
}
