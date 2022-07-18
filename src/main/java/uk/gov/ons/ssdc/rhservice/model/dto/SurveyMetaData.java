package uk.gov.ons.ssdc.rhservice.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class SurveyMetaData {
    SurveyData data;
    private List<String> receipting_keys;
}
