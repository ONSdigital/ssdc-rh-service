package uk.gov.ons.ssdc.rhservice.model.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.ons.ssdc.rhservice.model.dto.UacUpdateDTO;
import uk.gov.ons.ssdc.rhservice.service.RHFirestoreClient;

@ExtendWith(MockitoExtension.class)
class UacRepositoryTest {
  public static final String TEST_UAC_SCHEMA = "testUACchema";

  @Mock RHFirestoreClient rhFirestoreClient;

  @InjectMocks UacRepository uacRepository;

  @BeforeEach
  public void setUp() {
    ReflectionTestUtils.setField(uacRepository, "uacSchemaName", TEST_UAC_SCHEMA);
  }

  @Test
  void testWriteUACUpdate() {
    UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
    uacUpdateDTO.setCaseId(UUID.randomUUID().toString());
    uacUpdateDTO.setCollectionExerciseId(UUID.randomUUID().toString());
    uacUpdateDTO.setQid("000000000001");
    uacUpdateDTO.setUacHash("blah");

    uacRepository.writeUAC(uacUpdateDTO);
    verify(rhFirestoreClient).storeObject(TEST_UAC_SCHEMA, uacUpdateDTO.getUacHash(), uacUpdateDTO);
  }

  @Test
  void testRetrieve() {
    UacUpdateDTO uacUpdateDTO = new UacUpdateDTO();
    uacUpdateDTO.setCaseId(UUID.randomUUID().toString());
    uacUpdateDTO.setCollectionExerciseId(UUID.randomUUID().toString());
    uacUpdateDTO.setQid("000000000001");
    uacUpdateDTO.setUacHash("blah");

    when(rhFirestoreClient.retrieveObject(
            UacUpdateDTO.class, TEST_UAC_SCHEMA, uacUpdateDTO.getUacHash()))
        .thenReturn(Optional.of(uacUpdateDTO));

    Optional<UacUpdateDTO> actualUACOpt = uacRepository.readUAC(uacUpdateDTO.getUacHash());

    assertThat(actualUACOpt).isPresent();
    assertThat(actualUACOpt.get()).isEqualTo(uacUpdateDTO);
  }
}
