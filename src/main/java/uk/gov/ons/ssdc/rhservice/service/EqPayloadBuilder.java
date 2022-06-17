package uk.gov.ons.ssdc.rhservice.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.ons.ssdc.rhservice.crypto.JweEncryptor;
import uk.gov.ons.ssdc.rhservice.crypto.KeyStore;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.EqLaunchCoreData;
import uk.gov.ons.ssdc.rhservice.model.dto.EqLaunchData;

@Service
public class EqPayloadBuilder {
    private final JweEncryptor jweEncryptor;

    public EqPayloadBuilder(KeyStore keyStore) {
        this.jweEncryptor = new JweEncryptor(keyStore, "authentication");
    }

    public String buildPayLoadAndGetEqLaunchJwe(EqLaunchData launchData) {
        EqLaunchCoreData coreLaunchData = launchData.coreCopy();

        Map<String, Object> payload =
                createPayloadMap(
                        coreLaunchData,
                        launchData.getCaseUpdate(),
                        launchData.getUserId(),
                        null,
                        launchData.getAccountServiceUrl(),
                        launchData.getAccountServiceLogoutUrl());

        return jweEncryptor.encrypt(payload);
    }

    Map<String, Object> createPayloadMap(
            EqLaunchCoreData eqLaunchCoreData,
            CaseUpdateDTO caseUpdate,
            String userId,
            String role,
            String accountServiceUrl,
            String accountServiceLogoutUrl) {

        UUID caseId = UUID.fromString(caseUpdate.getCaseId());
        String questionnaireId = eqLaunchCoreData.getUacUpdateDTO().getQid();

        long currentTimeInSeconds = System.currentTimeMillis() / 1000;

        LinkedHashMap<String, Object> payload = new LinkedHashMap<>();

        payload.computeIfAbsent("jti", (k) -> UUID.randomUUID().toString());
        payload.computeIfAbsent("tx_id", (k) -> UUID.randomUUID().toString());
        payload.computeIfAbsent("iat", (k) -> currentTimeInSeconds);
        payload.computeIfAbsent("exp", (k) -> currentTimeInSeconds + (5 * 60));
        payload.computeIfAbsent("collection_exercise_sid",
                (k) -> caseUpdate.getCollectionExerciseId());

        verifyNotNull(caseUpdate.getCollectionExerciseId(), "collection id", caseId);
        verifyNotNull(questionnaireId, "questionnaireId", caseId);

        payload.computeIfAbsent("ru_ref", (k) -> questionnaireId);
        payload.computeIfAbsent("user_id", (k) -> userId);
        String caseIdStr = caseUpdate.getCaseId();
        payload.computeIfAbsent("case_id", (k) -> caseIdStr);
        payload.computeIfAbsent(
                "language_code", (k) -> eqLaunchCoreData.getLanguage().getIsoLikeCode());
        payload.computeIfAbsent("eq_id", (k) -> "9999");
        payload.computeIfAbsent("period_id", (k) -> caseUpdate.getCollectionExerciseId());
        payload.computeIfAbsent("form_type", (k) -> "zzz");
        payload.computeIfAbsent("schema_name", (k) -> "zzz_9999");
        payload.computeIfAbsent(
                "survey_url", (k) -> eqLaunchCoreData.getUacUpdateDTO().getCollectionInstrumentUrl());
        payload.computeIfAbsent("case_ref", (k) -> caseUpdate.getCaseRef());
        payload.computeIfAbsent("ru_name", (k) -> "West Efford Cottage, y y y ??");

        String responseId = encryptResponseId(questionnaireId, eqLaunchCoreData.getSalt());
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

    // Creates encrypted response id from SALT and questionnaireId
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

