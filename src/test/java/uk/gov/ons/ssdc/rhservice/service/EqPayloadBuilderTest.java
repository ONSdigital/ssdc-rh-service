package uk.gov.ons.ssdc.rhservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;

class EqPayloadBuilderTest {

  public static final String ACCOUNT_SERVICE_URL = "https://test.com";
  public static final String ACCOUNT_SERVICE_LOGOUT_URL = "https://test.com";
  public static final String LANGUAGE_CODE = "en";

  @Test
  void testBuildEqPayload() {
    // Given
    EqPayloadBuilder underTest = new EqPayloadBuilder("TEST_PEPPER");
    ReflectionTestUtils.setField(underTest, "responseIdPepper", "TEST");

    UacUpdateDTO uacUpdateDTO = getUacUpdate();
    CaseUpdateDTO caseUpdateDTO = getCaseUpdate(uacUpdateDTO);

    // When
    Map<String, Object> eqPayload =
        underTest.buildEqPayloadMap(
            ACCOUNT_SERVICE_URL,
            ACCOUNT_SERVICE_LOGOUT_URL,
            LANGUAGE_CODE,
            uacUpdateDTO,
            caseUpdateDTO);

    assertThat(eqPayload)
        .containsKey("jti")
        .containsKey("tx_id")
        .containsKey("iat")
        .containsKey("exp")
        .containsEntry("collection_exercise_sid", caseUpdateDTO.getCollectionExerciseId())
        .containsEntry("ru_ref", uacUpdateDTO.getQid())
        .containsEntry("user_id", "RH")
        .containsEntry("case_id", caseUpdateDTO.getCaseId())
        .containsEntry("language_code", LANGUAGE_CODE)
        .containsEntry("eq_id", "9999")
        .containsEntry("period_id", caseUpdateDTO.getCollectionExerciseId())
        .containsEntry("form_type", "zzz")
        .containsEntry("schema_name", "zzz_9999")
        .containsEntry("survey_url", uacUpdateDTO.getCollectionInstrumentUrl())
        .containsEntry("case_ref", caseUpdateDTO.getCaseRef())
        .containsEntry("response_id", "TEST_QID_a8410f66014e5778")
        .containsEntry("account_service_url", ACCOUNT_SERVICE_URL)
        .containsEntry("account_service_log_out_url", ACCOUNT_SERVICE_LOGOUT_URL)
        .containsEntry("channel", "rh")
        .containsEntry("questionnaire_id", uacUpdateDTO.getQid());

    long iat = (long) eqPayload.get("iat");
    Date iatDate = new Date(TimeUnit.SECONDS.toMillis(iat));
    OffsetDateTime iatOffsetDateTime = iatDate.toInstant().atOffset(ZoneOffset.UTC);

    long exp = (long) eqPayload.get("exp");
    Date expDate = new Date(TimeUnit.SECONDS.toMillis(exp));
    OffsetDateTime expOffsetDateTime = expDate.toInstant().atOffset(ZoneOffset.UTC);

    // TODO: These time assertions are brittle when debugging. We should consider mocking time if we
    // have further need for this
    assertThat(iatOffsetDateTime).isCloseTo(OffsetDateTime.now(), within(5, ChronoUnit.SECONDS));
    assertThat(expOffsetDateTime).isCloseTo(OffsetDateTime.now(), within(5, ChronoUnit.MINUTES));
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
                    ACCOUNT_SERVICE_URL,
                    ACCOUNT_SERVICE_LOGOUT_URL,
                    LANGUAGE_CODE,
                    uacUpdateDTO,
                    caseUpdateDTO));
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
                    ACCOUNT_SERVICE_URL,
                    ACCOUNT_SERVICE_LOGOUT_URL,
                    LANGUAGE_CODE,
                    uacUpdateDTO,
                    caseUpdateDTO));
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
                    ACCOUNT_SERVICE_URL,
                    ACCOUNT_SERVICE_LOGOUT_URL,
                    invalidLanguageCode,
                    uacUpdateDTO,
                    caseUpdateDTO));
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
}
