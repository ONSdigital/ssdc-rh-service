package uk.gov.ons.ssdc.rhservice.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class SurveyMetaData {
    //    TODO: add JSON mapping thing here, data is a bad name - is it a keyword
    private SurveyData data;
    private List<String> receipting_keys;
}
