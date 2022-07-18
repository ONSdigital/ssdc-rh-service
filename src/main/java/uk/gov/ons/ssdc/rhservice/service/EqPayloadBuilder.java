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
import uk.gov.ons.ssdc.rhservice.model.dto.CollectionExerciseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.EqLaunchTokenDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.SurveyMetaData;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;

@Service
public class EqPayloadBuilder {
    private static final Set<String> ALLOWED_LANGUAGE_CODES = Set.of("cy", "en");

    @Value("${eq.response-id-salt")
    private String responseIdSalt;

    public Map<String, Object> buildEqPayloadMap(
            String accountServiceUrl,
            String accountServiceLogoutUrl,
            String languageCode,
            UacUpdateDTO uacUpdateDTO,
            CaseUpdateDTO caseUpdateDTO, CollectionExerciseUpdateDTO collectionExerciseUpdateDTO) {

        validateData(caseUpdateDTO, uacUpdateDTO, languageCode);

        long currentTimeInSeconds = System.currentTimeMillis() / 1000;

        long expTime = currentTimeInSeconds + (5 * 60);

        EqLaunchTokenDTO eqLaunchTokenDTO = new EqLaunchTokenDTO();
        eqLaunchTokenDTO.setExp(expTime);
        eqLaunchTokenDTO.setIat(currentTimeInSeconds);
        eqLaunchTokenDTO.setJti(UUID.randomUUID().toString());
        eqLaunchTokenDTO.setTx_id(UUID.randomUUID().toString());
        eqLaunchTokenDTO.setAccount_service_url(accountServiceUrl);
        eqLaunchTokenDTO.setCase_id(caseUpdateDTO.getCaseId());
        eqLaunchTokenDTO.setChannel("RH");
        eqLaunchTokenDTO.setCollection_exercise_sid(collectionExerciseUpdateDTO.getCollectionExerciseId());
        eqLaunchTokenDTO.setLanguage_code(languageCode);

            // work for this in progress, needs salt n pepper
        eqLaunchTokenDTO.setResponse_id("XYZ");

        eqLaunchTokenDTO.setSchema_url(collectionExerciseUpdateDTO.getCollectionInstrumentSelectionRules());

        String schema_url;
        SurveyMetaData survey_metadata;



        payload.put("exp", expTime);
        payload.put("iat", currentTimeInSeconds);
        payload.put("jti", UUID.randomUUID().toString());


        payload.put("tx_id", UUID.randomUUID().toString());


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
                        uacUpdateDTO.getQid(), responseIdSalt));
        payload.put("account_service_url", accountServiceUrl);
        payload.put("account_service_log_out_url", accountServiceLogoutUrl);
        payload.put("channel", "rh");
        payload.put("questionnaire_id", uacUpdateDTO.getQid());

        return payload;
    }

  /*
  Copied from https://github.com/ONSdigital/ons-schema-definitions/blob/v3/docs/rm_to_eq_runner_payload_v2.rst

  Should we just make this a DTO, not sure of the benefit of a MAP.
  POJOs would be a lot clearer

  Comments added by me

  {
  "exp": 1458057712,                                            // currentTimeInSeconds
  "iat": 1458047712,                                            // currentTimeInSeconds + 5 minutes (300)
  "jti": "6b383088-b8f8-4167-8847-c4aaeda8fe16",                // a new UUID we can create
  "tx_id": "0f534ffc-9442-414c-b39f-a756b4adc6cb",              // UUID Transaction ID used to trace a transaction through the whole system.
                                                                // MUST NOT be the same as the 'jti' value.
  "account_service_url": "https://upstream.example.com",        // we already provide this, and logout??
  "case_id": "628256cf-5c78-4896-8bec-f0ddb69aaa11",            // we have this
  "channel": "RH",                                              // Just hardcode by us to RH
  "collection_exercise_sid": "789",                             // A reference UUID -< as per doc.  (not sure why example is 3 long).  We have this
  //"region_code": "GB-WLS",                                    // NOTE this is legacy.  we don't have this or need it for MVP
  "language_code",                                              // Optional (official) field added by me.  We have en/cy, and are legally required to support
  //"response_expires_at": "2022-12-01T00:00:00+00:00",         // Note: Optional, this is to delete partial responses after X time.
                                                                // Not MVP. This would have to be defined at Survey level if we wanted to set it for some surveys.
  "response_id": "QzXMrPqoLiyEyerrED88AbkQoQK0sVVX72ZtVphHr0w=", // work already specced for this: https://trello.com/c/2EYYMlvH/240-properly-handle-and-document-eq-launch-response-id-hashing-and-peppering-5
  "schema_name": "adhoc_0001",                                  // Or schema Url.  Will need collex DTO for this, and for the DTO to start including the CI rules
  "survey_metadata": {
    "data": {
      "case_ref": "1000000000000001",                              // we have this
      "case_type": "B",                                            // not applicable to us, particualalry for MVP.
      // Questionnaire_id will be a string of numbers, currently
      "questionnaire_id": "bdf7dff2-1d73-4b97-bd2d-91f2e53160b9"   // qid in the UacUpdateDTO
    },
    "receipting_keys": [
      "questionnaire_id"                                           // Can stay as is.
    ]
  }
}

   */

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

    private void validateLanguageCode(String languageCode) {
        if (!ALLOWED_LANGUAGE_CODES.contains(languageCode)) {
            throw new RuntimeException(String.format("Invalid language code: '%s'", languageCode));
        }
    }
}
