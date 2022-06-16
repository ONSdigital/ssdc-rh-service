package uk.gov.ons.ssdc.rhservice.service;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.ons.ssdc.rhservice.crypto.JweEncryptor;
import uk.gov.ons.ssdc.rhservice.crypto.KeyStore;
import uk.gov.ons.ssdc.rhservice.model.EqLaunchRequestDTO;
import uk.gov.ons.ssdc.rhservice.model.LaunchDataDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.EqLaunch;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.repository.CaseRepository;
import uk.gov.ons.ssdc.rhservice.model.repository.UacRepository;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class EqLaunchService {
    private static final String ROLE_FLUSHER = "flusher";

    private JweEncryptor codec;
    private final UacRepository uacRepository;
    private final CaseRepository caseRepository;

    public EqLaunchService(KeyStore keyStore, UacRepository uacRepository, CaseRepository caseRepository)  {
        this.codec = new JweEncryptor(keyStore, "authentication");
        this.uacRepository = uacRepository;
        this.caseRepository = caseRepository;
    }

    public String generateEqLaunchToken(String uacHash, EqLaunchRequestDTO eqLaunchedDTO) {

        // Build launch URL
        LaunchDataDTO launchData = gatherLaunchData(uacHash);
        String eqLaunchUrl = createLaunchToken(launchData, eqLaunchedDTO);

        // Publish the launch event
        EqLaunch eqLaunch = new EqLaunch();
        eqLaunch.setQid(launchData.getUacUpdateDTO().getQid());

        // TODO: IMPLEMENT this the SRM way
        //eventPublisher.sendEvent(TopicType.EQ_LAUNCH, Source.RESPONDENT_HOME, Channel.RH, eqLaunch);

        return eqLaunchUrl;
    }

    String createLaunchToken(LaunchDataDTO launchData, EqLaunchRequestDTO eqLaunchedDTO) {

        String encryptedToken = "";

        EqLaunchData eqLaunchData = new EqLaunchData();
        eqLaunchData.setLanguage(eqLaunchedDTO.getLanguageCode());
        eqLaunchData.setSource("RESPONDENT_HOME");
        eqLaunchData.setChannel("RH");
        eqLaunchData.setUacUpdateDTO(launchData.getUacUpdateDTO());
        eqLaunchData.setCaseUpdate(launchData.getCaseUpdateDTO());
        eqLaunchData.setUserId("RH");
        eqLaunchData.setAccountServiceUrl(eqLaunchedDTO.getAccountServiceUrl());
        eqLaunchData.setAccountServiceLogoutUrl(eqLaunchedDTO.getAccountServiceLogoutUrl());

        encryptedToken = getEqLaunchJwe(eqLaunchData);

        return encryptedToken;
    }

    private LaunchDataDTO gatherLaunchData(String uacHash)  {
        UacUpdateDTO uacUpdateDTO =
                    uacRepository
                        .readUAC(uacHash)
                        .orElseThrow(
                                () ->
                                        new RuntimeException("Failed to retrieve UAC"));

        String caseId = uacUpdateDTO.getCaseId();

        if (StringUtils.isEmpty(caseId)) {
            throw new RuntimeException("UAC has no caseId");
        }

        CaseUpdateDTO caseUpdateDTO =
                caseRepository
                        .readCaseUpdate(caseId)
                        .orElseThrow(() -> new RuntimeException("Case Not Found"));

        LaunchDataDTO launchData = new LaunchDataDTO();
        launchData.setCaseUpdateDTO(caseUpdateDTO);
        launchData.setUacUpdateDTO(uacUpdateDTO);

        return launchData;
    }


    //TODO, make this all work, not urgent yet - but needs completing for ticket.
//    private void sendUacAuthenticationEvent(String caseId, String qid) {
//
//        log.info(
//                "Generating UacAuthentication event for caseId",
//                kv("caseId", caseId),
//                kv("questionnaireId", qid));
//
//        UacAuthentication uacAuthentication = UacAuthentication.builder().qid(qid).build();
//
//        UUID messageId =
//                eventPublisher.sendEvent(
//                        TopicType.UAC_AUTHENTICATION, Source.RESPONDENT_HOME, Channel.RH, uacAuthentication);
//
//        log.debug(
//                "UacAuthentication event published for qid: "
//                        + uacAuthentication.getQid()
//                        + ", messageId: "
//                        + messageId);
//    }


    public String getEqLaunchJwe(EqLaunchData launchData) {
        EqLaunchCoreData coreLaunchData = launchData.coreCopy();

        Map<String, Object> payload =
                createPayloadMap(
                        coreLaunchData,
                        launchData.getCaseUpdate(),
                        launchData.getUserId(),
                        null,
                        launchData.getAccountServiceUrl(),
                        launchData.getAccountServiceLogoutUrl());

        return codec.encrypt(payload);
    }

    public String getEqFlushLaunchJwe(EqLaunchCoreData launchData) {

        Map<String, Object> payload =
                createPayloadMap(launchData, null, null, ROLE_FLUSHER, null, null);

        return codec.encrypt(payload);
    }

    Map<String, Object> createPayloadMap(EqLaunchCoreData coreData, CaseUpdateDTO caseUpdate,
                                         String userId, String role, String accountServiceUrl,
                                         String accountServiceLogoutUrl) {

        UUID caseId = UUID.fromString(caseUpdate.getCaseId());
        String questionnaireId = coreData.getUacUpdateDTO().getQid();

        long currentTimeInSeconds = System.currentTimeMillis() / 1000;

        LinkedHashMap<String, Object> payload = new LinkedHashMap<>();

        payload.computeIfAbsent("jti", (k) -> UUID.randomUUID().toString());
        payload.computeIfAbsent("tx_id", (k) -> UUID.randomUUID().toString());
        payload.computeIfAbsent("iat", (k) -> currentTimeInSeconds);
        payload.computeIfAbsent("exp", (k) -> currentTimeInSeconds + (5 * 60));
        payload.computeIfAbsent("collection_exercise_sid", (k) -> caseUpdate.getCollectionExerciseId());

        String convertedRegionCode = convertRegionCode(caseUpdate.getSample().get("region"));
        payload.computeIfAbsent("region_code", (k) -> convertedRegionCode);

        if (role == null || !role.equals(ROLE_FLUSHER)) {
            Objects.requireNonNull(
                    caseUpdate, "caseUpdate mandatory unless role is '" + ROLE_FLUSHER + "'");

            verifyNotNull(caseUpdate.getCollectionExerciseId(), "collection id", caseId);
            verifyNotNull(questionnaireId, "questionnaireId", caseId);

            payload.computeIfAbsent("ru_ref", (k) -> questionnaireId);
            payload.computeIfAbsent("user_id", (k) -> userId);
            String caseIdStr = caseUpdate.getCaseId();
            payload.computeIfAbsent("case_id", (k) -> caseIdStr);
            payload.computeIfAbsent("language_code", (k) -> coreData.getLanguage().getIsoLikeCode());
            payload.computeIfAbsent("eq_id", (k) -> "9999");
            payload.computeIfAbsent("period_id", (k) -> caseUpdate.getCollectionExerciseId());
            payload.computeIfAbsent("form_type", (k) -> "zzz");
            payload.computeIfAbsent("schema_name", (k) -> "zzz_9999");
            payload.computeIfAbsent(
                    "survey_url", (k) -> coreData.getUacUpdateDTO().getCollectionInstrumentUrl());
            payload.computeIfAbsent("case_ref", (k) -> caseUpdate.getCaseRef());
            payload.computeIfAbsent("ru_name", (k) -> "West Efford Cottage, y y y ??");
        }

        String responseId = encryptResponseId(questionnaireId, coreData.getSalt());
        payload.computeIfAbsent("response_id", (k) -> responseId);
        payload.computeIfAbsent("account_service_url", (k) -> accountServiceUrl);
        payload.computeIfAbsent("account_service_log_out_url", (k) -> accountServiceLogoutUrl);
        payload.computeIfAbsent("channel", (k) -> "rh");
        payload.computeIfAbsent("questionnaire_id", (k) -> questionnaireId);

        return payload;
    }

    private void verifyNotNull(Object fieldValue, String fieldName, UUID caseId) {
        if (fieldValue == null) {
            throw new RuntimeException("No value supplied for " + fieldName + " field of case " + caseId);
        }
    }

    private String convertRegionCode(String caseRegionStr) {
        String regionValue = "GB-ENG";

        if (caseRegionStr != null) {
            char caseRegion = caseRegionStr.charAt(0);
            if (caseRegion == 'N') {
                regionValue = "GB-NIR";
            } else if (caseRegion == 'W') {
                regionValue = "GB-WLS";
            } else if (caseRegion == 'E') {
                regionValue = "GB-ENG";
            }
        }

        return regionValue;
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
