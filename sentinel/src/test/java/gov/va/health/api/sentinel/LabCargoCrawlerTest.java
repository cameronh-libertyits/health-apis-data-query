package gov.va.health.api.sentinel;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.health.api.sentinel.categories.Manual;
import gov.va.health.api.sentinel.crawler.ConcurrentRequestQueue;
import gov.va.health.api.sentinel.crawler.Crawler;
import gov.va.health.api.sentinel.crawler.FileResultsCollector;
import gov.va.health.api.sentinel.crawler.RequestQueue;
import gov.va.health.api.sentinel.crawler.ResourceDiscovery;
import gov.va.health.api.sentinel.crawler.SummarizingResultCollector;
import gov.va.health.api.sentinel.crawler.UrlReplacementRequestQueue;
import java.io.File;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Slf4j
public class LabCargoCrawlerTest {

  private final String clinicianUser = System.getProperty("clinician-user","someuser");
  private final String clinicianPassword = System.getProperty("clinician-password","somepassword");

  private int crawl(String patient) {
    SystemDefinition env = Sentinel.get().system();
    Swiggity.swooty(patient);
    Supplier<String> accessToken = () -> basicToken();
    ResourceDiscovery discovery =
        ResourceDiscovery.builder()
            .patientId(patient)
            .url("https://dev-api.va.gov/services/argonaut/v0")
            .build();
    SummarizingResultCollector results =
        SummarizingResultCollector.wrap(
            new FileResultsCollector(new File("target/lab-cargo-crawl-" + patient)));
    RequestQueue q = requestQueue(env);
    discovery.queries().forEach(q::add);
    Crawler crawler =
        Crawler.builder()
            .executor(Executors.newFixedThreadPool(10))
            .requestQueue(q)
            .results(results)
            .authenticationScheme("Basic")
            .authenticationToken(accessToken)
            .forceJargonaut(true)
            .build();
    crawler.crawl();
    log.info(
        "Results for {} \n{}",
        patient,
        results.message());
    return results.failures();
  }

  @Category(Manual.class)
  @Test
  public void crawlPatients() {
    int failureCount = 0;
    String patientIds = System.getProperty("patient-id", "1017283132V631076");
    String[] patients = patientIds.split(",");
    assertThat(clinicianUser).isNotBlank();
    log.info("Clinician user is specified as {})", clinicianUser);
    assertThat(clinicianPassword).isNotBlank();
    log.info("Clinician password is specified)");
    for (String patient : patients) {
      failureCount += crawl(patient.trim());
    }
    assertThat(failureCount).withFailMessage("%d Failures", failureCount).isEqualTo(0);
  }

  private RequestQueue requestQueue(SystemDefinition env) {
    String replaceUrl = System.getProperty("sentinel.argonaut.url.replace");
    if (isBlank(replaceUrl)) {
      log.info("Link replacement disabled (Override with -Dsentinel.argonaut.url.replace=<url>)");
      return new ConcurrentRequestQueue();
    }
    log.info(
        "Link replacement {} (Override with -Dsentinel.argonaut.url.replace=<url>)", replaceUrl);
    return UrlReplacementRequestQueue.builder()
        .replaceUrl(replaceUrl)
        .withUrl(env.argonaut().url())
        .requestQueue(new ConcurrentRequestQueue())
        .build();
  }

  private String basicToken(){
    return Base64.getEncoder().encodeToString((clinicianUser+":"+clinicianPassword).getBytes());
  }
}
