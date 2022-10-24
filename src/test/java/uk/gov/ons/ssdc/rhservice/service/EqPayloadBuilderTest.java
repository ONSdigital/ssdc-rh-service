package uk.gov.ons.ssdc.rhservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;

class EqPayloadBuilderTest {

  public static final String ACCOUNT_SERVICE_URL = "https://test.com";
  public static final String LANGUAGE_CODE = "en";
  private static final String responseIdPepper = "TEST";

  @Test
  void testBuildEqPayload() {
    // Given
    EqPayloadBuilder underTest = new EqPayloadBuilder("TEST_PEPPER");
    ReflectionTestUtils.setField(underTest, "responseIdPepper", responseIdPepper);

    UacUpdateDTO uacUpdateDTO = getUacUpdate();
    CaseUpdateDTO caseUpdateDTO = getCaseUpdate(uacUpdateDTO);

    // When
    Map<String, Object> eqPayload =
        underTest.buildEqPayloadMap(
            ACCOUNT_SERVICE_URL, LANGUAGE_CODE, uacUpdateDTO, caseUpdateDTO);

    assertThat(secondsStringToDateTime((long) eqPayload.get("exp")))
        .isCloseTo(OffsetDateTime.now().plusMinutes(5), within(5, ChronoUnit.SECONDS));
    assertThat(secondsStringToDateTime((long) eqPayload.get("iat")))
        .isCloseTo(OffsetDateTime.now(), within(5, ChronoUnit.SECONDS));

    assertThat(UUID.fromString(eqPayload.get("jti").toString())).isNotNull();
    assertThat(UUID.fromString(eqPayload.get("tx_id").toString())).isNotNull();
    assertThat(UUID.fromString(eqPayload.get("tx_id").toString()))
        .isNotEqualTo((UUID.fromString(eqPayload.get("jti").toString())));

    assertThat(eqPayload.get("account_service_url")).isEqualTo(ACCOUNT_SERVICE_URL);

    assertThat(eqPayload.get("case_id")).isEqualTo(caseUpdateDTO.getCaseId());
    assertThat(eqPayload.get("channel")).isEqualTo("RH");
    assertThat(eqPayload.get("collection_exercise_sid"))
        .isEqualTo(caseUpdateDTO.getCollectionExerciseId());
    assertThat(eqPayload.get("language_code")).isEqualTo(LANGUAGE_CODE);
    assertThat(eqPayload.get("version")).isEqualTo("V2");
    assertThat(eqPayload.get("response_id").toString())
        .isEqualTo(getExpectedEncryptedResponseId(uacUpdateDTO.getQid()));
    assertThat(eqPayload.get("schema_name")).isEqualTo(uacUpdateDTO.getCollectionInstrumentUrl());

    Map<String, Object> actualSurveyMetaData =
        (Map<String, Object>) eqPayload.get("survey_metadata");
    Map<String, Object> actualData = (Map<String, Object>) actualSurveyMetaData.get("data");
    assertThat(actualData.get("qid")).isEqualTo(uacUpdateDTO.getQid());
    assertThat(actualSurveyMetaData.get("receipting_keys")).isEqualTo(List.of("qid"));
  }

  private OffsetDateTime secondsStringToDateTime(long actualSeconds) {
    Date actualIatDate = new Date(TimeUnit.SECONDS.toMillis(actualSeconds));
    return actualIatDate.toInstant().atOffset(ZoneOffset.UTC);
  }

  @Test
  void testValidateEmptyCollectionExerciseIdFailure() {
    EqPayloadBuilder underTest = new EqPayloadBuilder("TEST_PEPPER");

    // Given
    UacUpdateDTO uacUpdateDTO = getUacUpdate();
    CaseUpdateDTO caseUpdateDTO = getCaseUpdate(uacUpdateDTO);
    caseUpdateDTO.setCollectionExerciseId(null);

    // When, Then
    RuntimeException thrownException =
        assertThrows(
            RuntimeException.class,
            () ->
                underTest.buildEqPayloadMap(
                    ACCOUNT_SERVICE_URL, LANGUAGE_CODE, uacUpdateDTO, caseUpdateDTO));
    assertThat(thrownException.getMessage())
        .isEqualTo(
            String.format(
                "collectionExerciseId '%s' not found for caseId '%s'",
                caseUpdateDTO.getCollectionExerciseId(), uacUpdateDTO.getCaseId()));
  }

  @Test
  void testValidateEmptyQidFailure() {
    EqPayloadBuilder underTest = new EqPayloadBuilder("TEST_PEPPER");
    // Given
    UacUpdateDTO uacUpdateDTO = getUacUpdate();
    uacUpdateDTO.setQid(null);

    CaseUpdateDTO caseUpdateDTO = getCaseUpdate(uacUpdateDTO);
    Optional<CaseUpdateDTO> caseOpt = Optional.of(caseUpdateDTO);

    // When, Then
    RuntimeException thrownException =
        assertThrows(
            RuntimeException.class,
            () ->
                underTest.buildEqPayloadMap(
                    ACCOUNT_SERVICE_URL, LANGUAGE_CODE, uacUpdateDTO, caseUpdateDTO));
    assertThat(thrownException.getMessage())
        .isEqualTo(
            String.format(
                "QID '%s' not found for caseId '%s'",
                uacUpdateDTO.getQid(), caseUpdateDTO.getCaseId()));
  }

  @Test
  void testValidateLanguageCodeFailure() {
    EqPayloadBuilder underTest = new EqPayloadBuilder("TEST_PEPPER");
    // Given
    UacUpdateDTO uacUpdateDTO = getUacUpdate();

    CaseUpdateDTO caseUpdateDTO = getCaseUpdate(uacUpdateDTO);

    // When, Then
    String invalidLanguageCode = "Invalid Language code";
    RuntimeException thrownException =
        assertThrows(
            RuntimeException.class,
            () ->
                underTest.buildEqPayloadMap(
                    ACCOUNT_SERVICE_URL, invalidLanguageCode, uacUpdateDTO, caseUpdateDTO));
    assertThat(thrownException.getMessage())
        .isEqualTo(String.format("Invalid language code: '%s'", invalidLanguageCode));
  }

  private CaseUpdateDTO getCaseUpdate(UacUpdateDTO uacUpdate) {
    CaseUpdateDTO caseUpdate = new CaseUpdateDTO();

    caseUpdate.setCaseId(uacUpdate.getCaseId());
    caseUpdate.setSurveyId(uacUpdate.getSurveyId());
    caseUpdate.setCollectionExerciseId(uacUpdate.getCollectionExerciseId());
    caseUpdate.setInvalid(false);
    caseUpdate.setRefusalReceived(null);

    Map<String, String> sample = new HashMap<>();
    sample.put("ADDRESS_LINE1", "123 Fake Street");
    caseUpdate.setSample(sample);

    Map<String, String> sampleSensitive = new HashMap<>();
    sampleSensitive.put("Telephone", "020712345");
    caseUpdate.setSampleSensitive(sampleSensitive);

    caseUpdate.setCaseRef("CASE_REF");
    caseUpdate.setCreatedAt(new Date(System.currentTimeMillis()));
    caseUpdate.setLastUpdatedAt(null);

    return caseUpdate;
  }

  private UacUpdateDTO getUacUpdate() {
    UacUpdateDTO uacUpdate = new UacUpdateDTO();

    uacUpdate.setCaseId(UUID.randomUUID().toString());
    uacUpdate.setCollectionExerciseId(UUID.randomUUID().toString());
    uacUpdate.setSurveyId("TEST_SURVEY");
    uacUpdate.setCollectionInstrumentUrl("test.com");
    uacUpdate.setActive(true);
    uacUpdate.setUacHash("TEST_UAC_HASH");
    uacUpdate.setQid("TEST_QID");
    uacUpdate.setReceiptReceived(false);
    uacUpdate.setEqLaunched(false);

    return uacUpdate;
  }

  private String getExpectedEncryptedResponseId(String qid) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      md.update(responseIdPepper.getBytes());
      byte[] bytes = md.digest(qid.getBytes());
      return qid + "_" + new String(Hex.encode(bytes), 0, 16);
    } catch (NoSuchAlgorithmException ex) {
      throw new RuntimeException("No SHA-256 algorithm while encrypting qid", ex);
    }
  }
}
