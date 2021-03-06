package gov.va.api.health.dataquery.service.controller.medicationorder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import gov.va.api.health.argonaut.api.resources.MedicationOrder;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dstu2.api.bundle.BundleLink;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

@DataJpaTest
@RunWith(SpringRunner.class)
public class DatamartMedicationOrderControllerTest {

  @Autowired private TestEntityManager entity;

  @Autowired private MedicationOrderRepository repository;

  private IdentityService ids = mock(IdentityService.class);

  @SneakyThrows
  private MedicationOrderEntity asEntity(DatamartMedicationOrder dm) {
    return MedicationOrderEntity.builder()
        .cdwId(dm.cdwId())
        .icn(dm.patient().reference().get())
        .payload(JacksonConfig.createMapper().writeValueAsString(dm))
        .build();
  }

  MedicationOrderController controller() {
    return new MedicationOrderController(
        true,
        null,
        null,
        new Bundler(new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool")),
        repository,
        WitnessProtection.builder().identityService(ids).build());
  }

  @SneakyThrows
  String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  public void mockMedicationOrderIdentity(String publicId, String cdwId) {
    ResourceIdentity resourceIdentity =
        ResourceIdentity.builder()
            .identifier(cdwId)
            .resource("MEDICATION_ORDER")
            .system("CDW")
            .build();
    when(ids.lookup(publicId)).thenReturn(List.of(resourceIdentity));
    when(ids.register(any()))
        .thenReturn(
            List.of(
                Registration.builder().uuid(publicId).resourceIdentity(resourceIdentity).build()));
  }

  private Multimap<String, MedicationOrder> populateData() {
    var fhir = DatamartMedicationOrderSamples.Fhir.create();
    var datamart = DatamartMedicationOrderSamples.Datamart.create();
    var medicationOrderByPatient = LinkedHashMultimap.<String, MedicationOrder>create();
    var registrations = new ArrayList<Registration>(10);
    for (int i = 0; i < 10; i++) {
      var patientId = "p" + i % 2;
      var cdwId = "" + i;
      var publicId = "90" + i;
      var dm = datamart.medicationOrder(cdwId, patientId);
      repository.save(asEntity(dm));
      var medicationOrder = fhir.medicationOrder(publicId, patientId);
      medicationOrderByPatient.put(patientId, medicationOrder);
      ResourceIdentity resourceIdentity =
          ResourceIdentity.builder()
              .identifier(cdwId)
              .resource("MEDICATION_ORDER")
              .system("CDW")
              .build();
      Registration registration =
          Registration.builder().uuid(publicId).resourceIdentity(resourceIdentity).build();
      registrations.add(registration);
      when(ids.lookup(publicId)).thenReturn(List.of(resourceIdentity));
    }
    when(ids.register(any())).thenReturn(registrations);
    return medicationOrderByPatient;
  }

  @Test
  public void read() {
    DatamartMedicationOrder dm = DatamartMedicationOrderSamples.Datamart.create().medicationOrder();
    repository.save(asEntity(dm));
    mockMedicationOrderIdentity("1", dm.cdwId());
    MedicationOrder actual = controller().read("true", "1");
    assertThat(json(actual))
        .isEqualTo(json(DatamartMedicationOrderSamples.Fhir.create().medicationOrder("1")));
  }

  @Test
  public void readRaw() {
    DatamartMedicationOrder dm = DatamartMedicationOrderSamples.Datamart.create().medicationOrder();
    repository.save(asEntity(dm));
    mockMedicationOrderIdentity("1", dm.cdwId());
    String actual = controller().readRaw("1");
    assertThat(toObject(actual)).isEqualTo(dm);
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readRawThrowsNotFoundWhenDataIsMissing() {
    mockMedicationOrderIdentity("1", "1");
    controller().readRaw("1");
  }

  @Test(expected = ResourceExceptions.NotFound.class)
  public void readRawThrowsNotFoundWhenIdIsUnknown() {
    controller().readRaw("1");
  }

  @Test
  public void searchById() {
    DatamartMedicationOrder dm = DatamartMedicationOrderSamples.Datamart.create().medicationOrder();
    repository.save(asEntity(dm));
    mockMedicationOrderIdentity("1", dm.cdwId());
    MedicationOrder.Bundle actual = controller().searchById("true", "1", 1, 1);
    MedicationOrder medicationOrder =
        DatamartMedicationOrderSamples.Fhir.create()
            .medicationOrder("1", dm.patient().reference().get());
    assertThat(json(actual))
        .isEqualTo(
            json(
                DatamartMedicationOrderSamples.Fhir.asBundle(
                    "http://fonzy.com/cool",
                    List.of(medicationOrder),
                    DatamartMedicationOrderSamples.Fhir.link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/MedicationOrder?identifier=1",
                        1,
                        1),
                    DatamartMedicationOrderSamples.Fhir.link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/MedicationOrder?identifier=1",
                        1,
                        1),
                    DatamartMedicationOrderSamples.Fhir.link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/MedicationOrder?identifier=1",
                        1,
                        1))));
  }

  @Test
  public void searchByPatient() {
    Multimap<String, MedicationOrder> medicationOrderByPatient = populateData();
    assertThat(json(controller().searchByPatient("true", "p0", 1, 10)))
        .isEqualTo(
            json(
                DatamartMedicationOrderSamples.Fhir.asBundle(
                    "http://fonzy.com/cool",
                    medicationOrderByPatient.get("p0"),
                    DatamartMedicationOrderSamples.Fhir.link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/MedicationOrder?patient=p0",
                        1,
                        10),
                    DatamartMedicationOrderSamples.Fhir.link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/MedicationOrder?patient=p0",
                        1,
                        10),
                    DatamartMedicationOrderSamples.Fhir.link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/MedicationOrder?patient=p0",
                        1,
                        10))));
  }

  @SneakyThrows
  private DatamartMedicationOrder toObject(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartMedicationOrder.class);
  }
}
