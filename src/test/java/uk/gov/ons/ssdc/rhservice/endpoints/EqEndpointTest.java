//package uk.gov.ons.ssdc.rhservice.endpoints;
//
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//
//import java.util.Map;
//
//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.reset;
//import static org.mockito.Mockito.when;
//import static org.mockito.MockitoAnnotations.initMocks;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
//
//public class CaseEndpointUnitTest {
//    private MockMvc mockMvc;
//
////    @Mock
////    private CaseService caseService;
////    @Mock private UacQidServiceClient uacQidServiceClient;
//
//    @InjectMocks
//    private EqLaunchEndpoint underTest;
//
//    @BeforeEach
//    public void setUp() {
//        mockMvc = MockMvcBuilders.standaloneSetup(underTest).build();
//    }
//
//    @Test
//    public void getCaseReturnsExpectedCaseFields() throws Exception {
//        // Given
//        Case actualCase = createSingleCaseWithEvents();
//        when(caseService.findById(any())).thenReturn(actualCase);
//
//        // When
//        MvcResult result =
//                mockMvc
//                        .perform(
//                                get(DataUtils.createUrl("/cases/%s", TEST1_CASE_ID))
//                                        .accept(MediaType.APPLICATION_JSON))
//                        .andExpect(handler().handlerType(CaseEndpoint.class))
//                        .andExpect(handler().methodName(METHOD_NAME_FIND_CASE_BY_ID))
//                        .andReturn();
//
//        // Then
//        CaseContainerDTO responseCaseDTO =
//                mapper.readValue(result.getResponse().getContentAsString(), CaseContainerDTO.class);
//        Case responseCase = new Case();
//        responseCase.setCaseRef(Long.parseLong(responseCaseDTO.getCaseRef()));
//        responseCase.setId(responseCaseDTO.getId());
//        responseCase.setInvalid(responseCaseDTO.isInvalid());
//        responseCase.setCreatedAt(responseCaseDTO.getCreatedAt());
//        responseCase.setLastUpdatedAt(responseCaseDTO.getLastUpdatedAt());
//        responseCase.setRefusalReceived(responseCaseDTO.getRefusalReceived());
//        responseCase.setSample(responseCaseDTO.getSample());
//        assertThat(responseCase)
//                .isEqualToComparingOnlyGivenFields(
//                        actualCase, "id", "caseRef", "refusalReceived", "invalid", "sample");
//        assertThat(responseCase.getCreatedAt().toEpochSecond())
//                .isEqualTo(actualCase.getCreatedAt().toEpochSecond());
//        assertThat(responseCase.getLastUpdatedAt().toEpochSecond())
//                .isEqualTo(actualCase.getLastUpdatedAt().toEpochSecond());
//    }
