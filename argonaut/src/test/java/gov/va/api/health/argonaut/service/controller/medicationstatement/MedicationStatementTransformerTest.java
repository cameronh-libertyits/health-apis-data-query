package gov.va.api.health.argonaut.service.controller.medicationstatement;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.argonaut.api.datatypes.CodeableConcept;
import gov.va.api.health.argonaut.api.datatypes.Timing;
import gov.va.api.health.argonaut.api.elements.Reference;
import gov.va.api.health.argonaut.api.resources.MedicationStatement;
import gov.va.api.health.argonaut.api.resources.MedicationStatement.Dosage;
import gov.va.api.health.argonaut.api.resources.MedicationStatement.Status;
import gov.va.dvp.cdw.xsd.model.CdwCodeableConcept;
import gov.va.dvp.cdw.xsd.model.CdwMedicationStatement102Root.CdwMedicationStatements.CdwMedicationStatement;
import gov.va.dvp.cdw.xsd.model.CdwMedicationStatement102Root.CdwMedicationStatements.CdwMedicationStatement.CdwDosages;
import gov.va.dvp.cdw.xsd.model.CdwMedicationStatement102Root.CdwMedicationStatements.CdwMedicationStatement.CdwDosages.CdwDosage;
import gov.va.dvp.cdw.xsd.model.CdwMedicationStatement102Root.CdwMedicationStatements.CdwMedicationStatement.CdwDosages.CdwDosage.CdwTiming;
import gov.va.dvp.cdw.xsd.model.CdwMedicationStatementStatus;
import gov.va.dvp.cdw.xsd.model.CdwReference;
import java.math.BigInteger;
import java.util.List;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.junit.Test;

public class MedicationStatementTransformerTest {

  private final MedicationStatementTransformer tx = new MedicationStatementTransformer();
  private final CdwSampleData cdw = CdwSampleData.get();
  private final Expected expected = Expected.get();

  @Test
  public void medicationStatement() {
    assertThat(tx.apply(cdw.medicationStatement())).isEqualTo(expected.medicationStatement());
  }

  @Test
  public void dosage() {
    assertThat(tx.dosage(cdw.dosages())).isEqualTo(expected.dosage());
    assertThat(tx.dosage(null)).isNull();
    assertThat(tx.dosage(new CdwDosages())).isNull();
  }

  @Test
  public void codeableConcept() {
    assertThat(tx.codeableConcept(null)).isNull();
    assertThat(tx.codeableConcept(cdw.route())).isEqualTo(expected.route());
    assertThat(tx.codeableConcept(cdw.timingCode())).isEqualTo(expected.timingCode());
  }

  @Test
  public void reference() {
    assertThat(tx.reference(null)).isNull();
    assertThat(tx.reference(cdw.reference("x", "y"))).isEqualTo(expected.reference("x", "y"));
  }

  @Test
  public void status() {
    assertThat(tx.status(CdwMedicationStatementStatus.ACTIVE)).isEqualTo(Status.active);
    assertThat(tx.status(CdwMedicationStatementStatus.COMPLETED)).isEqualTo(Status.completed);
    assertThat(tx.status(CdwMedicationStatementStatus.ENTERED_IN_ERROR))
        .isEqualTo(Status.entered_in_error);
    assertThat(tx.status(CdwMedicationStatementStatus.INTENDED)).isEqualTo(Status.intended);
  }

  @NoArgsConstructor(staticName = "get")
  private static class CdwSampleData {
    private CdwMedicationStatement medicationStatement() {
      CdwMedicationStatement cdw = new CdwMedicationStatement();
      cdw.setCdwId("1400000182118");
      cdw.setStatus(CdwMedicationStatementStatus.COMPLETED);
      cdw.setDateAsserted(dateTime("2012-07-12T14:08:08Z"));
      cdw.setDosages(dosages());
      cdw.setEffectiveDateTime(dateTime("2012-07-12T14:08:08Z"));
      cdw.setMedicationReference(reference("Medication/420489", "ALLOPURINOL 100MG TAB"));
      cdw.setNote("reports obtaining privately");
      cdw.setRowNumber(BigInteger.ONE);
      cdw.setPatient(reference("Patient/185601V825290", "VETERAN,JOHN Q"));
      return cdw;
    }

    @SneakyThrows
    private XMLGregorianCalendar dateTime(String timestamp) {
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(timestamp);
    }

    private CdwReference reference(String ref, String display) {
      CdwReference cdw = new CdwReference();
      cdw.setReference(ref);
      cdw.setDisplay(display);
      return cdw;
    }

    private CdwDosages dosages() {
      CdwDosages cdw = new CdwDosages();
      cdw.getDosage().add(dosage());
      return cdw;
    }

    private CdwDosage dosage() {
      CdwDosage cdw = new CdwDosage();
      cdw.setRoute(route());
      cdw.setText("100MG");
      cdw.setTiming(timing());
      return cdw;
    }

    private CdwCodeableConcept route() {
      CdwCodeableConcept cdw = new CdwCodeableConcept();
      cdw.setText("BY MOUTH");
      return cdw;
    }

    private CdwTiming timing() {
      CdwTiming cdw = new CdwTiming();
      cdw.setCode(timingCode());
      return cdw;
    }

    private CdwCodeableConcept timingCode() {
      CdwCodeableConcept cdw = new CdwCodeableConcept();
      cdw.setText("EVERY DAY");
      return cdw;
    }
  }

  @NoArgsConstructor(staticName = "get")
  private static class Expected {

    private MedicationStatement medicationStatement() {
      return MedicationStatement.builder()
          .resourceType("MedicationStatement")
          .id("1400000182118")
          .status(Status.completed)
          .patient(reference("Patient/185601V825290", "VETERAN,JOHN Q"))
          .note("reports obtaining privately")
          .medicationReference(reference("Medication/420489", "ALLOPURINOL 100MG TAB"))
          .effectiveDateTime("2012-07-12T14:08:08Z")
          .dosage(dosage())
          .dateAsserted("2012-07-12T14:08:08Z")
          .build();
    }

    private Reference reference(String ref, String display) {
      return Reference.builder().reference(ref).display(display).build();
    }

    private List<Dosage> dosage() {
      return singletonList(Dosage.builder().route(route()).text("100MG").timing(timing()).build());
    }

    private CodeableConcept route() {
      return CodeableConcept.builder().text("BY MOUTH").build();
    }

    private Timing timing() {
      return Timing.builder().code(timingCode()).build();
    }

    private CodeableConcept timingCode() {
      return CodeableConcept.builder().text("EVERY DAY").build();
    }
  }
}