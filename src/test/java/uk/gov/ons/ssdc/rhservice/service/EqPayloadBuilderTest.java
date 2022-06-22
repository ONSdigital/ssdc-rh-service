package uk.gov.ons.ssdc.rhservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.repository.CaseRepository;
import uk.gov.ons.ssdc.rhservice.model.repository.UacRepository;

@ExtendWith(MockitoExtension.class)
class EqPayloadBuilderTest {

  @Mock UacRepository uacRepository;

  @Mock CaseRepository caseRepository;

  public static final String ACCOUNT_SERVICE_URL = "https://test.com";
  public static final String ACCOUNT_SERVICE_LOGOUT_URL = "https://test.com";
  public static final String LANGUAGE_CODE = "en";

  @InjectMocks EqPayloadBuilder underTest;

  @Test
  void testBuildEqPayload() {
    // Given
    ReflectionTestUtils.setField(underTest, "responseIdSalt", "TEST");

    UacUpdateDTO uacUpdate = getUacUpdate();
    Optional<UacUpdateDTO> uacOpt = Optional.of(uacUpdate);
    when(uacRepository.readUAC(uacUpdate.getUacHash())).thenReturn(uacOpt);

    CaseUpdateDTO caseUpdate = getCaseUpdate(uacUpdate);
    Optional<CaseUpdateDTO> caseOpt = Optional.of(caseUpdate);
    when(caseRepository.readCaseUpdate(uacUpdate.getCaseId())).thenReturn(caseOpt);

    // When
    Map<String, Object> eqPayload =
        underTest.buildEqPayloadMap(
            uacUpdate.getUacHash(), ACCOUNT_SERVICE_URL, ACCOUNT_SERVICE_LOGOUT_URL, LANGUAGE_CODE);

    // Then
    String expectedEncryptedResponseId = "TEST_QIDa8410f66014e5778";
    assertThat(eqPayload)
        .containsKey("jti")
        .containsKey("tx_id")
        .containsKey("iat")
        .containsKey("exp")
        .containsEntry("collection_exercise_sid", caseUpdate.getCollectionExerciseId())
        .containsEntry("ru_ref", uacUpdate.getQid())
        .containsEntry("user_id", "RH")
        .containsEntry("case_id", caseUpdate.getCaseId())
        .containsEntry("language_code", LANGUAGE_CODE)
        .containsEntry("eq_id", "9999")
        .containsEntry("period_id", caseUpdate.getCollectionExerciseId())
        .containsEntry("form_type", "zzz")
        .containsEntry("schema_name", "zzz_9999")
        .containsEntry("survey_url", uacUpdate.getCollectionInstrumentUrl())
        .containsEntry("case_ref", caseUpdate.getCaseRef())
        .containsEntry("response_id", expectedEncryptedResponseId)
        .containsEntry("account_service_url", ACCOUNT_SERVICE_URL)
        .containsEntry("account_service_log_out_url", ACCOUNT_SERVICE_LOGOUT_URL)
        .containsEntry("channel", "rh")
        .containsEntry("questionnaire_id", uacUpdate.getQid());
  }

  @Test
  void testGetUacFromHashFailure() {
    // When, Then
    RuntimeException thrownException =
        assertThrows(
            RuntimeException.class,
            () ->
                underTest.buildEqPayloadMap(
                    "UAC_HASH", ACCOUNT_SERVICE_URL, ACCOUNT_SERVICE_LOGOUT_URL, LANGUAGE_CODE));
    assertThat(thrownException.getMessage()).isEqualTo("Failed to retrieve UAC");
    verifyNoInteractions(caseRepository);
  }

  @Test
  void testGetCaseWithEmptyCaseIdFailure() {
    // Given
    UacUpdateDTO uacUpdate = getUacUpdate();
    uacUpdate.setCaseId(null);
    Optional<UacUpdateDTO> uacOpt = Optional.of(uacUpdate);
    when(uacRepository.readUAC(uacUpdate.getUacHash())).thenReturn(uacOpt);

    // When, Then
    String uacHash = uacUpdate.getUacHash();
    RuntimeException thrownException =
        assertThrows(
            RuntimeException.class,
            () ->
                underTest.buildEqPayloadMap(
                    uacHash, ACCOUNT_SERVICE_URL, ACCOUNT_SERVICE_LOGOUT_URL, LANGUAGE_CODE));
    assertThat(thrownException.getMessage()).isEqualTo("UAC has no caseId");
    verifyNoInteractions(caseRepository);
  }

  @Test
  void testCaseNotFoundFailure() {
    // Given
    UacUpdateDTO uacUpdate = getUacUpdate();
    Optional<UacUpdateDTO> uacOpt = Optional.of(uacUpdate);
    when(uacRepository.readUAC(uacUpdate.getUacHash())).thenReturn(uacOpt);

    // When, Then
    String uacHash = uacUpdate.getUacHash();
    RuntimeException thrownException =
        assertThrows(
            RuntimeException.class,
            () ->
                underTest.buildEqPayloadMap(
                    uacHash, ACCOUNT_SERVICE_URL, ACCOUNT_SERVICE_LOGOUT_URL, LANGUAGE_CODE));
    assertThat(thrownException.getMessage())
        .isEqualTo(String.format("caseId '%s' not found", uacUpdate.getCaseId()));
  }

  @Test
  void testValidateEmptyCollectionExerciseIdFailure() {
    // Given
    UacUpdateDTO uacUpdate = getUacUpdate();
    Optional<UacUpdateDTO> uacOpt = Optional.of(uacUpdate);
    when(uacRepository.readUAC(uacUpdate.getUacHash())).thenReturn(uacOpt);

    CaseUpdateDTO caseUpdate = getCaseUpdate(uacUpdate);
    caseUpdate.setCollectionExerciseId(null);
    Optional<CaseUpdateDTO> caseOpt = Optional.of(caseUpdate);
    when(caseRepository.readCaseUpdate(uacUpdate.getCaseId())).thenReturn(caseOpt);

    // When, Then
    String uacHash = uacUpdate.getUacHash();
    RuntimeException thrownException =
        assertThrows(
            RuntimeException.class,
            () ->
                underTest.buildEqPayloadMap(
                    uacHash, ACCOUNT_SERVICE_URL, ACCOUNT_SERVICE_LOGOUT_URL, LANGUAGE_CODE));
    assertThat(thrownException.getMessage())
        .isEqualTo(
            String.format(
                "collectionExerciseId '%s' not found for caseId '%s'",
                caseUpdate.getCollectionExerciseId(), uacUpdate.getCaseId()));
  }

  @Test
  void testValidateEmptyQidFailure() {
    // Given
    UacUpdateDTO uacUpdate = getUacUpdate();
    uacUpdate.setQid(null);
    Optional<UacUpdateDTO> uacOpt = Optional.of(uacUpdate);
    when(uacRepository.readUAC(uacUpdate.getUacHash())).thenReturn(uacOpt);

    CaseUpdateDTO caseUpdate = getCaseUpdate(uacUpdate);
    Optional<CaseUpdateDTO> caseOpt = Optional.of(caseUpdate);
    when(caseRepository.readCaseUpdate(uacUpdate.getCaseId())).thenReturn(caseOpt);

    // When, Then
    String uacHash = uacUpdate.getUacHash();
    RuntimeException thrownException =
        assertThrows(
            RuntimeException.class,
            () ->
                underTest.buildEqPayloadMap(
                    uacHash, ACCOUNT_SERVICE_URL, ACCOUNT_SERVICE_LOGOUT_URL, LANGUAGE_CODE));
    assertThat(thrownException.getMessage())
        .isEqualTo(
            String.format(
                "QID '%s' not found for caseId '%s'", uacUpdate.getQid(), caseUpdate.getCaseId()));
  }

  @Test
  void testValidateLanguageCodeFailure() {
    // Given
    UacUpdateDTO uacUpdate = getUacUpdate();
    Optional<UacUpdateDTO> uacOpt = Optional.of(uacUpdate);
    when(uacRepository.readUAC(uacUpdate.getUacHash())).thenReturn(uacOpt);

    CaseUpdateDTO caseUpdate = getCaseUpdate(uacUpdate);
    Optional<CaseUpdateDTO> caseOpt = Optional.of(caseUpdate);
    when(caseRepository.readCaseUpdate(uacUpdate.getCaseId())).thenReturn(caseOpt);

    // When, Then
    String uacHash = uacUpdate.getUacHash();
    String invalidLanguageCode = "Invalid Language code";
    RuntimeException thrownException =
        assertThrows(
            RuntimeException.class,
            () ->
                underTest.buildEqPayloadMap(
                    uacHash, ACCOUNT_SERVICE_URL, ACCOUNT_SERVICE_LOGOUT_URL, invalidLanguageCode));
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
