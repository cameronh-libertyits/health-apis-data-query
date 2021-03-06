package gov.va.api.health.dataquery.service.controller.conformance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@SuppressWarnings("DefaultAnnotationParam")
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("conformance")
@Data
@Accessors(fluent = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConformanceStatementProperties {
  private String id;
  private String version;
  private String name;
  private String publisher;
  private StatementType statementType;
  private ContactProperties contact;
  private String publicationDate;
  private String description;
  private String softwareName;
  private String fhirVersion;
  private SecurityProperties security;
  private String resourceDocumentation;

  enum StatementType {
    CLINICIAN,
    PATIENT
  }

  @Data
  @Accessors(fluent = false)
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class ContactProperties {
    private String name;
    private String email;
  }

  @Data
  @Accessors(fluent = false)
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class SecurityProperties {
    private String tokenEndpoint;
    private String authorizeEndpoint;
    private String description;
  }
}
