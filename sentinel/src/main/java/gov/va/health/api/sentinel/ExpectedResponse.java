package gov.va.health.api.sentinel;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import io.restassured.response.Response;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * A decorator for the standard Rest Assured response that adds a little more error support, by
 * automatically logging everything if an validation occurs.
 */
@Value
@AllArgsConstructor(staticName = "of")
@Slf4j
class ExpectedResponse {

  Response response;

  /** Expect the HTTP status code to be the given value. */
  ExpectedResponse expect(int statusCode) {
    try {
      response.then().statusCode(statusCode);
    } catch (AssertionError e) {
      log();
      throw e;
    }
    return this;
  }

  /**
   * Expect the body to be JSON represented by the given type, using the project standard {@link
   * JacksonConfig} object mapper.
   */
  private <T> T expect(Class<T> type) {
    try {
      return JacksonConfig.createMapper().readValue(response().asByteArray(), type);
    } catch (IOException e) {
      log();
      throw new AssertionError("Failed to parse JSON body", e);
    }
  }

  /** Expect no newline or tab characters within JSON body. */
  ExpectedResponse expectNoNewlineOrTabChars() {
    if (response.getBody().asString().contains("/n")
        || response.getBody().asString().contains("/t")) {
      throw new AssertionError("Newline or Tab characters exist inside of JSON body");
    }
    return this;
  }

  /**
   * Expect the body to be a JSON list represented by the given type, using the project standard
   * {@link JacksonConfig} object mapper.
   */
  <T> List<T> expectListOf(Class<T> type) {
    try {
      ObjectMapper mapper = JacksonConfig.createMapper();
      return mapper.readValue(
          response().asByteArray(),
          mapper.getTypeFactory().constructCollectionType(List.class, type));
    } catch (IOException e) {
      log();
      throw new AssertionError("Failed to parse JSON body", e);
    }
  }

  /**
   * Expect the body to be JSON represented by the given type, using the project standard {@link
   * JacksonConfig} object mapper, then perform Javax Validation against it.
   */
  <T> T expectValid(Class<T> type) {
    T payload = expect(type);
    Set<ConstraintViolation<T>> violations =
        Validation.buildDefaultValidatorFactory().getValidator().validate(payload);
    if (violations.isEmpty()) {
      return payload;
    }
    log();
    StringBuilder message = new StringBuilder("Constraint Violations:");
    violations.forEach(
        v ->
            message
                .append('\n')
                .append(v.getMessage())
                .append(": ")
                .append(v.getPropertyPath().toString())
                .append(" = ")
                .append(v.getInvalidValue()));
    message.append("\n\nDetails:");
    violations.forEach(v -> message.append('\n').append(v));
    throw new AssertionError(message.toString());
  }

  @SuppressWarnings("UnusedReturnValue")
  ExpectedResponse log() {
    response().then().log().all();
    return this;
  }
}
