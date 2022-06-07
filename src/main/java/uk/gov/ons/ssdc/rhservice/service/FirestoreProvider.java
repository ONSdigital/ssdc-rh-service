package uk.gov.ons.ssdc.rhservice.service;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FirestoreProvider {
  @Value("${spring.cloud.gcp.firestore.project-id}")
  private String gcpProject;

  private Firestore firestore;

  @PostConstruct
  public void create() {
    firestore = FirestoreOptions.newBuilder().setProjectId(gcpProject).build().getService();
  }

  public Firestore get() {
    return firestore;
  }
}
