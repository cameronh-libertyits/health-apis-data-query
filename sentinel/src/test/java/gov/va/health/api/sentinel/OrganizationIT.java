package gov.va.health.api.sentinel;

import static gov.va.health.api.sentinel.ResourceVerifier.test;

import gov.va.api.health.argonaut.api.resources.OperationOutcome;
import gov.va.api.health.argonaut.api.resources.Organization;
import gov.va.health.api.sentinel.categories.Prod;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class OrganizationIT {
  ResourceVerifier verifier = ResourceVerifier.get();

  @Test
  @Category({Prod.class})
  public void basic() {
    verifier.verifyAll(
        test(200, Organization.class, "/api/Organization/{id}", verifier.ids().organization()),
        test(404, OperationOutcome.class, "/api/Organization/{id}", verifier.ids().unknown()),
        test(
            200,
            Organization.Bundle.class,
            "/api/Organization?_id={id}",
            verifier.ids().organization()),
        test(
            200,
            Organization.Bundle.class,
            "/api/Organization?identifier={id}",
            verifier.ids().organization()),
        test(404, OperationOutcome.class, "/api/Organization?_id={id}", verifier.ids().unknown()));
  }
}
