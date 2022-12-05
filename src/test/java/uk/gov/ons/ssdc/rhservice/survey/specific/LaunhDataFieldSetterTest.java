// package uk.gov.ons.ssdc.rhservice.survey.specific;
//
// import static org.assertj.core.api.Assertions.assertThat;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;
//
// import java.util.HashMap;
// import java.util.Map;
// import java.util.Optional;
// import java.util.UUID;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import uk.gov.ons.ssdc.rhservice.model.dto.CaseUpdateDTO;
// import uk.gov.ons.ssdc.rhservice.model.dto.CollectionExerciseUpdateDTO;
// import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;
// import uk.gov.ons.ssdc.rhservice.model.repository.CaseRepository;
// import uk.gov.ons.ssdc.rhservice.model.repository.CollectionExerciseRepository;
//
// @ExtendWith(MockitoExtension.class)
// class LaunhDataFieldSetterTest {
//  private static final String CASE_ID = UUID.randomUUID().toString();
//  private static final String SURVEY_ID = UUID.randomUUID().toString();
//  private static final String COLLEX_ID = UUID.randomUUID().toString();
//
//  @Mock CaseRepository caseRepository;
//
//  @Mock CollectionExerciseRepository collectionExerciseRepository;
//
//  @InjectMocks LaunhDataFieldSetter underTest;
//
//  @Test
//  public void testNoMetaDataOnSurveyDoesntUpdateUACMetaData() {
//    UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
//    uacUpdateDTO.setCaseId(CASE_ID);
//
//    CaseUpdateDTO caseUpdateDTO = new CaseUpdateDTO();
//    caseUpdateDTO.setCaseId(CASE_ID);
//    caseUpdateDTO.setCollectionExerciseId(COLLEX_ID);
//
//    CollectionExerciseUpdateDTO collectionExerciseUpdateDTO = new CollectionExerciseUpdateDTO();
//    collectionExerciseUpdateDTO.setSurveyId(SURVEY_ID);
//
//    SurveyUpdateDto surveyDto = new SurveyUpdateDto();
//    surveyDto.setSurveyId(SURVEY_ID);
//
//    when(caseRepository.readCaseUpdate(any())).thenReturn(Optional.of(caseUpdateDTO));
//    when(collectionExerciseRepository.readCollectionExerciseUpdate(any()))
//        .thenReturn(Optional.of(collectionExerciseUpdateDTO));
//    when(surveyRepository.readSurveyUpdate(any())).thenReturn(Optional.of(surveyDto));
//
//    assertThat(uacUpdateDTO.getLaunchData()).isNull();
//    // when
//    underTest.stampLaunchDataFieldsOnUAC(uacUpdateDTO);
//    // then
//    assertThat(uacUpdateDTO.getLaunchData()).isNull();
//
//    verify(caseRepository).readCaseUpdate(CASE_ID);
//    verify(surveyRepository).readSurveyUpdate(SURVEY_ID);
//    verify(collectionExerciseRepository).readCollectionExerciseUpdate(COLLEX_ID);
//  }
//
//  @Test
//  public void testMetaDataButNotLaunchDataOnSurveyDoesntUpdateUACMetaData() {
//    UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
//    uacUpdateDTO.setCaseId(CASE_ID);
//
//    CaseUpdateDTO caseUpdateDTO = new CaseUpdateDTO();
//    caseUpdateDTO.setCaseId(CASE_ID);
//    caseUpdateDTO.setCollectionExerciseId(COLLEX_ID);
//
//    CollectionExerciseUpdateDTO collectionExerciseUpdateDTO = new CollectionExerciseUpdateDTO();
//    collectionExerciseUpdateDTO.setSurveyId(SURVEY_ID);
//
//    SurveyUpdateDto surveyDto = new SurveyUpdateDto();
//    surveyDto.setSurveyId(SURVEY_ID);
//
//    Map<String, Object> metaData = new HashMap<>();
//    Map<String, String> notLaunchDataSettings = new HashMap<>();
//    notLaunchDataSettings.put("field", "value1");
//    metaData.put("notLaunchData", notLaunchDataSettings);
//
//    surveyDto.setMetadata(metaData);
//
//    when(caseRepository.readCaseUpdate(any())).thenReturn(Optional.of(caseUpdateDTO));
//    when(collectionExerciseRepository.readCollectionExerciseUpdate(any()))
//        .thenReturn(Optional.of(collectionExerciseUpdateDTO));
//    when(surveyRepository.readSurveyUpdate(any())).thenReturn(Optional.of(surveyDto));
//
//    assertThat(uacUpdateDTO.getLaunchData()).isNull();
//    // when
//    underTest.stampLaunchDataFieldsOnUAC(uacUpdateDTO);
//    // then
//    assertThat(uacUpdateDTO.getLaunchData()).isNull();
//
//    verify(caseRepository).readCaseUpdate(CASE_ID);
//    verify(surveyRepository).readSurveyUpdate(SURVEY_ID);
//    verify(collectionExerciseRepository).readCollectionExerciseUpdate(COLLEX_ID);
//  }
//  // Test needs fixing
//  //  @Test
//  //  public void testWithLaunchDataSetUpdatesValues() {
//  //    UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
//  //    uacUpdateDTO.setCaseId(CASE_ID);
//  //
//  //    CaseUpdateDTO caseUpdateDTO = new CaseUpdateDTO();
//  //    caseUpdateDTO.setCaseId(CASE_ID);
//  //    caseUpdateDTO.setCollectionExerciseId(COLLEX_ID);
//  //    Map<String, String> sample = new HashMap<>();
//  //    sample.put("PARTICIPANT_ID", "123");
//  //    sample.put("NAME_FIRST", "FRED");
//  //    caseUpdateDTO.setSample(sample);
//  //
//  //    CollectionExerciseUpdateDTO collectionExerciseUpdateDTO = new
// CollectionExerciseUpdateDTO();
//  //    collectionExerciseUpdateDTO.setSurveyId(SURVEY_ID);
//  //
//  //    // idea for a nested set for Survey MetaData launchData - makes it's more flexible
//  //    LaunchDataFieldDTO launchDataFieldDTO1 = new LaunchDataFieldDTO();
//  //    launchDataFieldDTO1.setSampleField("PARTICIPANT_ID");
//  //    launchDataFieldDTO1.setLaunchDataFieldName("participantId");
//  //    launchDataFieldDTO1.setMandatory(true);
//  //
//  //    LaunchDataFieldDTO launchDataFieldDTO2 = new LaunchDataFieldDTO();
//  //    launchDataFieldDTO2.setSampleField("NAME_FIRST");
//  //    launchDataFieldDTO2.setLaunchDataFieldName("firstName");
//  //    launchDataFieldDTO2.setMandatory(true);
//  //
//  //    Map<String, Object> launchDataSettings = new HashMap<>();
//  //    launchDataSettings.put("launchDataSettings", List.of(launchDataFieldDTO1,
//  // launchDataFieldDTO2));
//  //
//  //    SurveyUpdateDto surveyDto = new SurveyUpdateDto();
//  //    surveyDto.setSurveyId(SURVEY_ID);
//  //    surveyDto.setMetadata(launchDataSettings);
//  //
//  //    when(caseRepository.readCaseUpdate(any())).thenReturn(Optional.of(caseUpdateDTO));
//  //    when(collectionExerciseRepository.readCollectionExerciseUpdate(any()))
//  //        .thenReturn(Optional.of(collectionExerciseUpdateDTO));
//  //    when(surveyRepository.readSurveyUpdate(any())).thenReturn(Optional.of(surveyDto));
//  //
//  //    assertThat(uacUpdateDTO.getLaunchData()).isNull();
//  //    // when
//  //    underTest.stampLaunchDataFieldsOnUAC(uacUpdateDTO);
//  //    // then
//  //    assertThat(uacUpdateDTO.getLaunchData()).isNotNull();
//  //
//  //    assertThat(uacUpdateDTO.getLaunchData().size()).isEqualTo(2);
//  //    assertThat(uacUpdateDTO.getLaunchData().get("participantId")).isEqualTo("123");
//  //    assertThat(uacUpdateDTO.getLaunchData().get("firstName")).isEqualTo("FRED");
//  //
//  //    verify(caseRepository).readCaseUpdate(CASE_ID);
//  //    verify(surveyRepository).readSurveyUpdate(SURVEY_ID);
//  //    verify(collectionExerciseRepository).readCollectionExerciseUpdate(COLLEX_ID);
//  //  }
// }
