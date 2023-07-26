package uk.gov.ons.ssdc.rhservice.service;

import static uk.gov.ons.ssdc.rhservice.utils.Constants.RESPONSE_EXPIRES_AT_WEEK_INCREMENT;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.CollectionExerciseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;

@Service
public class EqPayloadBuilder {

  private static final Set<String> ALLOWED_LANGUAGE_CODES = Set.of("cy", "en");
  public static final String EQ_SCHEMA_VERSION = "v2";

  private final String responseIdPepper;

  public EqPayloadBuilder(@Value("${eq.response-id-pepper}") String peppery) {
    this.responseIdPepper = peppery;
  }

  public Map<String, Object> buildEqPayloadMap(
      String accountServiceUrl,
      String languageCode,
      UacUpdateDTO uacUpdateDTO,
      CaseUpdateDTO caseUpdateDTO,
      CollectionExerciseUpdateDTO collectionExerciseUpdateDTO) {

    validateData(caseUpdateDTO, uacUpdateDTO, languageCode);

    long currentTimeInSeconds = System.currentTimeMillis() / 1000;
    long expTime = currentTimeInSeconds + (5 * 60);

    /*
     Schema from: https://github.com/ONSdigital/ons-schema-definitions/blob/v3/docs/rm_to_eq_runner_payload_v2.rst

     Why you may ask are we using a Map rather than a lovely POJO?
     Well the encryption library plays nicely with Maps, and is tried and tested that way.
    */
    Map<String, Object> eqTokenPayload = new HashMap<>();
    eqTokenPayload.put("exp", expTime);
    eqTokenPayload.put("iat", currentTimeInSeconds);
    eqTokenPayload.put("jti", UUID.randomUUID().toString());
    eqTokenPayload.put("tx_id", UUID.randomUUID().toString());
    eqTokenPayload.put("account_service_url", accountServiceUrl);
    eqTokenPayload.put("case_id", caseUpdateDTO.getCaseId());
    eqTokenPayload.put("channel", "RH");
    eqTokenPayload.put("collection_exercise_sid", caseUpdateDTO.getCollectionExerciseId());
    eqTokenPayload.put("language_code", languageCode);
    eqTokenPayload.put("version", EQ_SCHEMA_VERSION);
    eqTokenPayload.put("response_id", encryptResponseId(uacUpdateDTO.getQid()));
    // TODO: this is a tactical hack, we'd believed that the preferred schemaUrl field would be
    // populated
    // TODO: ticket to do this properly: https://trello.com/c/XEEIGcJW
    eqTokenPayload.put("schema_name", uacUpdateDTO.getCollectionInstrumentUrl());
    eqTokenPayload.put("survey_metadata", getSurveyMetaData(uacUpdateDTO));

    String weeksInTheFutureFromEndDate =
        collectionExerciseUpdateDTO
            .getEndDate()
            .toInstant()
            .atOffset(ZoneOffset.UTC)
            .plusWeeks(RESPONSE_EXPIRES_AT_WEEK_INCREMENT)
            .toString();
    eqTokenPayload.put("response_expires_at", weeksInTheFutureFromEndDate);

    return eqTokenPayload;
  }

  private Map<String, Object> getSurveyMetaData(UacUpdateDTO uacUpdateDTO) {
    Map<String, String> data = new HashMap<>();
    data.put("qid", uacUpdateDTO.getQid());

    if (uacUpdateDTO.getLaunchData() != null) {
      data.putAll(uacUpdateDTO.getLaunchData());
    }

    Map<String, Object> surveyMetaData = new HashMap<>();
    surveyMetaData.put("data", data);
    surveyMetaData.put("receipting_keys", List.of("qid"));

    return surveyMetaData;
  }

  private void validateData(
      CaseUpdateDTO caseUpdateDTO, UacUpdateDTO uacUpdateDTO, String languageCode) {
    String caseId = caseUpdateDTO.getCaseId();

    String qid = uacUpdateDTO.getQid();
    if (StringUtils.isEmpty(qid)) {
      throw new RuntimeException(String.format("QID '%s' not found for caseId '%s'", qid, caseId));
    }

    validateLanguageCode(languageCode);
  }

  /*
   Note: yes this returns the plaintext qid and a hash of the qid
   There is/was a valid downstream/EQ reason for doing this.  They also encrypt this field fully their end
  */
  private String encryptResponseId(String qid) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      md.update(responseIdPepper.getBytes());
      byte[] bytes = md.digest(qid.getBytes());
      return qid + "_" + new String(Hex.encode(bytes), 0, 16);
    } catch (NoSuchAlgorithmException ex) {
      throw new RuntimeException("No SHA-256 algorithm while encrypting qid", ex);
    }
  }

  private void validateLanguageCode(String languageCode) {
    if (!ALLOWED_LANGUAGE_CODES.contains(languageCode)) {
      throw new RuntimeException(String.format("Invalid language code: '%s'", languageCode));
    }
  }
}
