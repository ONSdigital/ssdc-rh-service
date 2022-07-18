package uk.gov.ons.ssdc.rhservice.model.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class EqLaunchTokenDTO {
    long exp;
    long iat;
    String jti;
    String tx_id;
    String account_service_url;
    String case_id;
    String channel;
    String collection_exercise_sid;
    String language_code;
    String response_id;
    String schema_url;
    String version;
    SurveyMetaData survey_metadata;
}