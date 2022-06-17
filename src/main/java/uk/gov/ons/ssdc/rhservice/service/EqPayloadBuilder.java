package uk.gov.ons.ssdc.rhservice.service;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.ons.ssdc.rhservice.model.EqLaunchRequestDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.repository.CaseRepository;
import uk.gov.ons.ssdc.rhservice.model.repository.UacRepository;
import uk.gov.ons.ssdc.rhservice.utils.Language;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class EqPayloadBuilder {
    @Value("${eq.response-id-salt")
    private String responseIdSalt;

    private final UacRepository uacRepository;
    private final CaseRepository caseRepository;

    public EqPayloadBuilder(UacRepository uacRepository,
                            CaseRepository caseRepository) {
        this.uacRepository = uacRepository;
        this.caseRepository = caseRepository;
    }

    public Map<String, Object> buildEqPayloadMap(String uacHash, EqLaunchRequestDTO eqLaunchedDTO) {
        UacUpdateDTO uacUpdateDTO = getUacFromHash(uacHash);
        CaseUpdateDTO caseUpdateDTO = getCaseFromUac(uacUpdateDTO.getCaseId());

        return createPayloadMap(
                uacUpdateDTO, caseUpdateDTO, eqLaunchedDTO.getAccountServiceUrl(),
                eqLaunchedDTO.getAccountServiceLogoutUrl(), eqLaunchedDTO.getLanguageCode()
        );
    }

    private CaseUpdateDTO getCaseFromUac(String caseId) {
        if (StringUtils.isEmpty(caseId)) {
            throw new RuntimeException("UAC has no caseId");
        }

        return caseRepository
                .readCaseUpdate(caseId)
                .orElseThrow(() -> new RuntimeException("Case Not Found"));
    }

    private UacUpdateDTO getUacFromHash(String uacHash) {
        UacUpdateDTO uacUpdateDTO =
                uacRepository
                        .readUAC(uacHash)
                        .orElseThrow(() -> new RuntimeException("Failed to retrieve UAC"));
        
        return uacUpdateDTO;
    }


    private Map<String, Object> createPayloadMap(UacUpdateDTO uacUpdateDTO, CaseUpdateDTO caseUpdateDTO,
                                                 String accountServiceUrl, String accountServiceLogoutUrl,
                                                 Language languageCode) {

        UUID caseId = UUID.fromString(caseUpdateDTO.getCaseId());
        String questionnaireId = uacUpdateDTO.getQid();

        long currentTimeInSeconds = System.currentTimeMillis() / 1000;

        LinkedHashMap<String, Object> payload = new LinkedHashMap<>();

        payload.computeIfAbsent("jti", (k) -> UUID.randomUUID().toString());
        payload.computeIfAbsent("tx_id", (k) -> UUID.randomUUID().toString());
        payload.computeIfAbsent("iat", (k) -> currentTimeInSeconds);
        payload.computeIfAbsent("exp", (k) -> currentTimeInSeconds + (5 * 60));
        payload.computeIfAbsent("collection_exercise_sid",
                (k) -> caseUpdateDTO.getCollectionExerciseId());

        verifyNotNull(caseUpdateDTO.getCollectionExerciseId(), "collection id", caseId);
        verifyNotNull(questionnaireId, "questionnaireId", caseId);

        payload.computeIfAbsent("ru_ref", (k) -> questionnaireId);
        payload.computeIfAbsent("user_id", (k) -> "RH");
        String caseIdStr = caseUpdateDTO.getCaseId();
        payload.computeIfAbsent("case_id", (k) -> caseIdStr);
        payload.computeIfAbsent(
                "language_code", (k) -> languageCode.getIsoLikeCode());
        payload.computeIfAbsent("eq_id", (k) -> "9999");
        payload.computeIfAbsent("period_id", (k) -> caseUpdateDTO.getCollectionExerciseId());
        payload.computeIfAbsent("form_type", (k) -> "zzz");
        payload.computeIfAbsent("schema_name", (k) -> "zzz_9999");
        payload.computeIfAbsent(
                "survey_url", (k) -> uacUpdateDTO.getCollectionInstrumentUrl());
        payload.computeIfAbsent("case_ref", (k) -> caseUpdateDTO.getCaseRef());
        payload.computeIfAbsent("ru_name", (k) -> "West Efford Cottage, y y y ??");

        String responseId = encryptResponseId(questionnaireId, responseIdSalt);
        payload.computeIfAbsent("response_id", (k) -> responseId);
        payload.computeIfAbsent("account_service_url", (k) -> accountServiceUrl);
        payload.computeIfAbsent("account_service_log_out_url", (k) -> accountServiceLogoutUrl);
        payload.computeIfAbsent("channel", (k) -> "rh");
        payload.computeIfAbsent("questionnaire_id", (k) -> questionnaireId);

        return payload;
    }

    private void verifyNotNull(Object fieldValue, String fieldName, UUID caseId) {
        if (fieldValue == null) {
            throw new RuntimeException(
                    "No value supplied for " + fieldName + " field of case " + caseId);
        }
    }

    private String encryptResponseId(String questionnaireId, String salt) {
        StringBuilder responseId = new StringBuilder(questionnaireId);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] bytes = md.digest(questionnaireId.getBytes());
            responseId.append((new String(Hex.encode(bytes)).substring(0, 16)));
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("No SHA-256 algorithm while encrypting questionnaire", ex);
        }
        return responseId.toString();
    }
}

