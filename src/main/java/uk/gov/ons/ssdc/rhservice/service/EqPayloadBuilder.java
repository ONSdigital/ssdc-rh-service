package uk.gov.ons.ssdc.rhservice.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.repository.CaseRepository;
import uk.gov.ons.ssdc.rhservice.model.repository.UacRepository;

@Service
public class EqPayloadBuilder {
  private static final Set<String> ALLOWED_LANGUAGE_CODES = Set.of("cy", "en");

  @Value("${eq.response-id-salt")
  private String responseIdSalt;

  private final UacRepository uacRepository;
  private final CaseRepository caseRepository;

  public EqPayloadBuilder(UacRepository uacRepository, CaseRepository caseRepository) {
    this.uacRepository = uacRepository;
    this.caseRepository = caseRepository;
  }

  public Map<String, Object> buildEqPayloadMap(
      String uacHash,
      String accountServiceUrl,
      String accountServiceLogoutUrl,
      String languageCode) {
    UacUpdateDTO uacUpdateDTO = getUacFromHash(uacHash);
    CaseUpdateDTO caseUpdateDTO = getCaseFromUac(uacUpdateDTO.getCaseId());
    validateData(caseUpdateDTO, uacUpdateDTO, languageCode);

    long currentTimeInSeconds = System.currentTimeMillis() / 1000;

    long expTime = currentTimeInSeconds + (5 * 60);

    LinkedHashMap<String, Object> payload = new LinkedHashMap<>();

    payload.put("jti", UUID.randomUUID().toString());
    payload.put("tx_id", UUID.randomUUID().toString());
    payload.put("iat", currentTimeInSeconds);
    payload.put("exp", expTime);
    payload.put("collection_exercise_sid", caseUpdateDTO.getCollectionExerciseId());

    payload.put("ru_ref", uacUpdateDTO.getQid());
    payload.put("user_id", "RH");
    payload.put("case_id", caseUpdateDTO.getCaseId());
    payload.put("language_code", languageCode);
    payload.put("eq_id", "9999");
    payload.put("period_id", caseUpdateDTO.getCollectionExerciseId());
    payload.put("form_type", "zzz");
    payload.put("schema_name", "zzz_9999");
    payload.put("survey_url", uacUpdateDTO.getCollectionInstrumentUrl());
    payload.put("case_ref", caseUpdateDTO.getCaseRef());

    payload.put("response_id", encryptResponseId(uacUpdateDTO.getQid(), responseIdSalt));
    payload.put("account_service_url", accountServiceUrl);
    payload.put("account_service_log_out_url", accountServiceLogoutUrl);
    payload.put("channel", "rh");
    payload.put("questionnaire_id", uacUpdateDTO.getQid());

    return payload;
  }

  private void validateData(
      CaseUpdateDTO caseUpdateDTO, UacUpdateDTO uacUpdateDTO, String languageCode) {
    String collectionExerciseId = caseUpdateDTO.getCollectionExerciseId();
    String caseId = caseUpdateDTO.getCaseId();

    if (StringUtils.isEmpty(collectionExerciseId)) {
      throw new RuntimeException(
          String.format(
              "collectionExerciseId '%s' not found for caseId '%s'", collectionExerciseId, caseId));
    }

    String qid = uacUpdateDTO.getQid();
    if (StringUtils.isEmpty(qid)) {
      throw new RuntimeException(String.format("QID '%s' not found for caseId '%s'", qid, caseId));
    }

    validateLanguageCode(languageCode);
  }

  private String encryptResponseId(String questionnaireId, String salt) {
    StringBuilder responseId = new StringBuilder(questionnaireId);
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      md.update(salt.getBytes());
      byte[] bytes = md.digest(questionnaireId.getBytes());
      responseId.append((new String(Hex.encode(bytes))), 0, 16);
    } catch (NoSuchAlgorithmException ex) {
      throw new RuntimeException("No SHA-256 algorithm while encrypting questionnaire", ex);
    }
    return responseId.toString();
  }

  private CaseUpdateDTO getCaseFromUac(String caseId) {
    if (StringUtils.isEmpty(caseId)) {
      throw new RuntimeException("UAC has no caseId");
    }

    return caseRepository
        .readCaseUpdate(caseId)
        .orElseThrow(() -> new RuntimeException(String.format("caseId '%s' not found", caseId)));
  }

  private UacUpdateDTO getUacFromHash(String uacHash) {
    return uacRepository
        .readUAC(uacHash)
        .orElseThrow(() -> new RuntimeException("Failed to retrieve UAC"));
  }

  private void validateLanguageCode(String languageCode) {
    if (!ALLOWED_LANGUAGE_CODES.contains(languageCode)) {
      throw new RuntimeException(String.format("Invalid language code: '%s'", languageCode));
    }
  }
}
