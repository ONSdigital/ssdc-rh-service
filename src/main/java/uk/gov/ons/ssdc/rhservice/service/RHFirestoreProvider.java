package uk.gov.ons.ssdc.rhservice.service;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RHFirestoreProvider {
  @Value("${firestore.project-id}")
  private String gcpProject;

  //  TODO: This shouldn't be in production code, feels dirty
  @Value("${firestore.emulator-host:}")
  private String emulatorHost;

  private Firestore firestore;

  @PostConstruct
  public void create() {
    var firestoreBuilder = FirestoreOptions.newBuilder().setProjectId(gcpProject);
    if (emulatorHost != null && !emulatorHost.isEmpty()) {
      firestoreBuilder.setEmulatorHost(emulatorHost);
    }
    firestore = firestoreBuilder.build().getService();
  }

  public Firestore get() {
    return firestore;
  }
}
