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

@Service
public class EqPayloadBuilder {

  private static final Set<String> ALLOWED_LANGUAGE_CODES = Set.of("cy", "en");

  private String responseIdPepper;

  public EqPayloadBuilder(@Value("${eq.response-id-pepper}") String peppery) {
    this.responseIdPepper = peppery;
  }

  public Map<String, Object> buildEqPayloadMap(
      String accountServiceUrl,
      String accountServiceLogoutUrl,
      String languageCode,
      UacUpdateDTO uacUpdateDTO,
      CaseUpdateDTO caseUpdateDTO) {

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

    payload.put(
        "response_id",
        encryptResponseId(
            uacUpdateDTO.getQid(), responseIdPepper)); // TODO: Is encrypting this necessary?
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

  // TODO: Do we even need to do this? We need to understand why this is might be needed
  private String encryptResponseId(String questionnaireId, String pepper) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      md.update(pepper.getBytes());
      byte[] bytes = md.digest(questionnaireId.getBytes());
      return questionnaireId + "_" + new String(Hex.encode(bytes), 0, 16);
    } catch (NoSuchAlgorithmException ex) {
      throw new RuntimeException("No SHA-256 algorithm while encrypting questionnaire", ex);
    }
  }

  private void validateLanguageCode(String languageCode) {
    if (!ALLOWED_LANGUAGE_CODES.contains(languageCode)) {
      throw new RuntimeException(String.format("Invalid language code: '%s'", languageCode));
    }
  }
}
