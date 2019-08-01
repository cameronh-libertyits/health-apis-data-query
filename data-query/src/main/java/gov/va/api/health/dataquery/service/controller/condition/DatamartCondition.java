package gov.va.api.health.dataquery.service.controller.condition;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import java.time.Instant;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatamartCondition {
  @Builder.Default private String objectType = "Condition";
  @Builder.Default private String objectVersion = "1";
  private Instant etlDate;
  private String cdwId;
  private DatamartReference patient;
  private Optional<DatamartReference> encounter;
  private Optional<DatamartReference> asserter;
  private Optional<Instant> dateRecorded;
  private Optional<SnomedCode> snomed;
  private Optional<IcdCode> icd;
  private Category category;
  private ClinicalStatus clinicalStatus;
  private Optional<Instant> onsetDateTime;
  private Optional<Instant> abatementDateTime;

  /** Lazy initialization with empty. */
  public Optional<Instant> abatementDateTime() {
    if (abatementDateTime == null) {
      abatementDateTime = Optional.empty();
    }
    return abatementDateTime;
  }

  /** Lazy initialization with empty. */
  public Optional<DatamartReference> asserter() {
    if (asserter == null) {
      asserter = Optional.empty();
    }
    return asserter;
  }

  /** Lazy initialization with empty. */
  public Optional<Instant> dateRecorded() {
    if (dateRecorded == null) {
      dateRecorded = Optional.empty();
    }
    return dateRecorded;
  }

  /** Lazy initialization with empty. */
  public Optional<DatamartReference> encounter() {
    if (encounter == null) {
      encounter = Optional.empty();
    }
    return encounter;
  }

  /** Lazy initialization with empty. */
  public Optional<IcdCode> icd() {
    if (icd == null) {
      icd = Optional.empty();
    }
    return icd;
  }

  /** Lazy initialization with empty. */
  public Optional<Instant> onsetDateTime() {
    if (onsetDateTime == null) {
      onsetDateTime = Optional.empty();
    }
    return onsetDateTime;
  }

  /** Lazy initialization with empty. */
  public Optional<SnomedCode> snomed() {
    if (snomed == null) {
      snomed = Optional.empty();
    }
    return snomed;
  }

  public enum ClinicalStatus {
    active,
    resolved
  }

  public enum Category {
    diagnosis,
    problem
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static class IcdCode {
    String code;
    String display;
    String version;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static class SnomedCode {
    String code;
    String display;
  }
}