package uk.gov.ons.ssdc.rhservice.model.repository;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.ons.ssdc.rhservice.model.dto.SurveyUpdateDto;
import uk.gov.ons.ssdc.rhservice.service.RHFirestoreClient;

@Service
public class SurveyRepository {
  private final RHFirestoreClient rhFirestoreClient;

  @Value("${cloud-storage.survey-schema-name}")
  private String surveychemaName;

  public SurveyRepository(RHFirestoreClient rhFirestoreClient) {
    this.rhFirestoreClient = rhFirestoreClient;
  }

  public void writeSurveyUpdate(final SurveyUpdateDto surveyDto) {
    rhFirestoreClient.storeObject(surveychemaName, surveyDto.getSurveyId(), surveyDto);
  }

  public Optional<SurveyUpdateDto> readSurveyUpdate(String surveyId) {
    return rhFirestoreClient.retrieveObject(SurveyUpdateDto.class, surveychemaName, surveyId);
  }
}
