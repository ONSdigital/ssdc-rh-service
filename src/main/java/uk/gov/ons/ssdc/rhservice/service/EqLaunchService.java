package uk.gov.ons.ssdc.rhservice.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
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
import uk.gov.ons.ssdc.rhservice.model.dto.EqLaunchCoreData;
import uk.gov.ons.ssdc.rhservice.model.dto.EqLaunchData;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;
import uk.gov.ons.ssdc.rhservice.model.repository.CaseRepository;
import uk.gov.ons.ssdc.rhservice.model.repository.UacRepository;

@Service
public class EqLaunchService {
  private static final String ROLE_FLUSHER = "flusher";

  @Value("${eq.response-id-salt")
  private String responseIdSale;

  private final EqPayloadBuilder eqPayloadBuilder;
  private final UacRepository uacRepository;
  private final CaseRepository caseRepository;

  public EqLaunchService(
      EqPayloadBuilder eqPayloadBuilder, UacRepository uacRepository, CaseRepository caseRepository) {
    this.eqPayloadBuilder = eqPayloadBuilder;
    this.uacRepository = uacRepository;
    this.caseRepository = caseRepository;
  }

  public String generateEqLaunchToken(String uacHash, EqLaunchRequestDTO eqLaunchedDTO) {

    // Build launch URL
    LaunchDataDTO launchData = gatherLaunchData(uacHash);
    String eqLaunchToken = createLaunchToken(launchData, eqLaunchedDTO);

    // Publish the launch event
    EqLaunch eqLaunch = new EqLaunch();
    eqLaunch.setQid(launchData.getUacUpdateDTO().getQid());

    // TODO: IMPLEMENT this the SRM way
    // eventPublisher.sendEvent(TopicType.EQ_LAUNCH, Source.RESPONDENT_HOME, Channel.RH, eqLaunch);

    return eqLaunchToken;
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
    eqLaunchData.setSalt(responseIdSale);

    encryptedToken = eqPayloadBuilder.getEqLaunchJwe(eqLaunchData);

    return encryptedToken;
  }

  private LaunchDataDTO gatherLaunchData(String uacHash) {
    UacUpdateDTO uacUpdateDTO =
        uacRepository
            .readUAC(uacHash)
            .orElseThrow(() -> new RuntimeException("Failed to retrieve UAC"));

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

  // TODO, make this all work, not urgent yet - but needs completing for ticket.
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
  //                        TopicType.UAC_AUTHENTICATION, Source.RESPONDENT_HOME, Channel.RH,
  // uacAuthentication);
  //
  //        log.debug(
  //                "UacAuthentication event published for qid: "
  //                        + uacAuthentication.getQid()
  //                        + ", messageId: "
  //                        + messageId);
  //    }




}
