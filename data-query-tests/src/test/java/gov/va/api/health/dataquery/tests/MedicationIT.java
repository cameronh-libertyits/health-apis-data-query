package gov.va.api.health.dataquery.tests;

import static gov.va.api.health.dataquery.tests.ResourceVerifier.test;

import gov.va.api.health.argonaut.api.resources.Medication;
import gov.va.api.health.dataquery.tests.categories.LabDataQueryClinician;
import gov.va.api.health.dataquery.tests.categories.LabDataQueryPatient;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryClinician;
import gov.va.api.health.dataquery.tests.categories.ProdDataQueryPatient;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.categories.Local;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class MedicationIT {
  ResourceVerifier verifier = ResourceVerifier.get();

  @Test
  @Category({Local.class, LabDataQueryClinician.class, ProdDataQueryClinician.class})
  public void advanced() {
    verifier.verifyAll(
        test(200, Medication.Bundle.class, "Medication?_id={id}", verifier.ids().medication()),
        test(404, OperationOutcome.class, "Medication?_id={id}", verifier.ids().unknown()),
        test(
            200,
            Medication.Bundle.class,
            "Medication?identifier={id}",
            verifier.ids().medication()));
  }

  @Test
  @Category({
    Local.class,
    LabDataQueryPatient.class,
    LabDataQueryClinician.class,
    ProdDataQueryPatient.class,
    ProdDataQueryClinician.class
  })
  public void basic() {
    verifier.verifyAll(
        test(200, Medication.class, "Medication/{id}", verifier.ids().medication()),
        test(404, OperationOutcome.class, "Medication/{id}", verifier.ids().unknown()));
  }
}
