package gov.va.api.health.dataquery.service.controller;

import static gov.va.api.health.dataquery.service.controller.Transformers.asCodeableConceptWrapping;
import static gov.va.api.health.dataquery.service.controller.Transformers.asCoding;
import static gov.va.api.health.dataquery.service.controller.Transformers.asDateString;
import static gov.va.api.health.dataquery.service.controller.Transformers.asDateTimeString;
import static gov.va.api.health.dataquery.service.controller.Transformers.asInteger;
import static gov.va.api.health.dataquery.service.controller.Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.Transformers.convert;
import static gov.va.api.health.dataquery.service.controller.Transformers.convertAll;
import static gov.va.api.health.dataquery.service.controller.Transformers.emptyToNull;
import static gov.va.api.health.dataquery.service.controller.Transformers.firstPayloadItem;
import static gov.va.api.health.dataquery.service.controller.Transformers.hasPayload;
import static gov.va.api.health.dataquery.service.controller.Transformers.ifPresent;
import static gov.va.api.health.dataquery.service.controller.Transformers.isBlank;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.Transformers.MissingPayload;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.elements.Reference;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.SneakyThrows;
import org.junit.Test;

public class TransformersTest {
  @Test
  public void allBlank() {
    assertThat(Transformers.allBlank()).isTrue();
    assertThat(Transformers.allBlank(null, null, null, null)).isTrue();
    assertThat(Transformers.allBlank(null, "", " ")).isTrue();
    assertThat(Transformers.allBlank(null, 1, null, null)).isFalse();
    assertThat(Transformers.allBlank(1, "x", "z", 2.0)).isFalse();
  }

  @Test
  public void asCodeableConceptWrappingReturnsNullIfCodingCannotBeConverted() {
    assertThat(asCodeableConceptWrapping(null)).isNull();
  }

  @Test
  public void asCodeableConceptWrappingReturnsValueIfCodingCanBeConverted() {
    assertThat(
            asCodeableConceptWrapping(
                DatamartCoding.of().system("s").code("c").display("d").build()))
        .isEqualTo(
            CodeableConcept.builder()
                .coding(List.of(Coding.builder().system("s").code("c").display("d").build()))
                .build());
  }

  @Test
  public void asCodingReturnsNullWhenOptionalHasNoValues() {
    assertThat(asCoding(Optional.of(DatamartCoding.of().build()))).isNull();
  }

  @Test
  public void asCodingReturnsNullWhenOptionalIsEmpty() {
    assertThat(asCoding(Optional.empty())).isNull();
  }

  @Test
  public void asCodingReturnsNullWhenOptionalIsNull() {
    assertThat(asCoding((Optional<DatamartCoding>) null)).isNull();
  }

  @Test
  public void asCodingReturnsNullWhenValueIsNull() {
    assertThat(asCoding((DatamartCoding) null)).isNull();
  }

  @Test
  public void asCodingReturnsValueWhenOptionalIsPresent() {
    assertThat(
            asCoding(Optional.of(DatamartCoding.of().system("s").code("c").display("d").build())))
        .isEqualTo(Coding.builder().system("s").code("c").display("d").build());
  }

  @Test
  public void asDateStringReturnsNullWhenCalendarIsNull() {
    assertThat(asDateString((XMLGregorianCalendar) null)).isNull();
  }

  @Test
  public void asDateStringReturnsNullWhenInstantIsNull() {
    assertThat(asDateString((LocalDate) null)).isNull();
  }

  @Test
  public void asDateStringReturnsNullWhenOptionalInstantIsEmpty() {
    assertThat(asDateString(Optional.empty())).isNull();
  }

  @Test
  public void asDateStringReturnsNullWhenOptionalInstantIsNull() {
    assertThat(asDateString((Optional<LocalDate>) null)).isNull();
  }

  @SneakyThrows
  @Test
  public void asDateStringReturnsStringWhenCalendarIsNotNull() {
    XMLGregorianCalendar time =
        DatatypeFactory.newInstance().newXMLGregorianCalendar(2005, 1, 21, 7, 57, 0, 0, 0);
    assertThat(asDateString(time)).isEqualTo("2005-01-21");
  }

  @Test
  public void asDateStringReturnsStringWhenInstantIsNotNull() {
    LocalDate time = LocalDate.parse("2005-01-21");
    assertThat(asDateString(time)).isEqualTo("2005-01-21");
  }

  @Test
  public void asDateStringReturnsStringWhenOptionalInstantIsNotNull() {
    LocalDate time = LocalDate.parse("2005-01-21");
    assertThat(asDateString(Optional.of(time))).isEqualTo("2005-01-21");
  }

  @Test
  public void asDateTimeStringReturnsNullWhenCalendarIsNull() {
    assertThat(asDateTimeString((XMLGregorianCalendar) null)).isNull();
  }

  @Test
  public void asDateTimeStringReturnsNullWhenInstantIsNull() {
    assertThat(asDateTimeString((Instant) null)).isNull();
  }

  @Test
  public void asDateTimeStringReturnsNullWhenOptionalInstantIsEmpty() {
    assertThat(asDateTimeString(Optional.empty())).isNull();
  }

  @Test
  public void asDateTimeStringReturnsNullWhenOptionalInstantIsNull() {
    assertThat(asDateTimeString((Optional<Instant>) null)).isNull();
  }

  @SneakyThrows
  @Test
  public void asDateTimeStringReturnsStringWhenCalendarIsNotNull() {
    XMLGregorianCalendar time =
        DatatypeFactory.newInstance().newXMLGregorianCalendar(2005, 1, 21, 7, 57, 0, 0, 0);
    assertThat(asDateTimeString(time)).isEqualTo("2005-01-21T07:57:00.000Z");
  }

  @Test
  public void asDateTimeStringReturnsStringWhenInstantIsNotNull() {
    Instant time = Instant.parse("2005-01-21T07:57:00.000Z");
    assertThat(asDateTimeString(time)).isEqualTo("2005-01-21T07:57:00Z");
  }

  @Test
  public void asDateTimeStringReturnsStringWhenOptionalInstantIsPresent() {
    Instant time = Instant.parse("2005-01-21T07:57:00.000Z");
    assertThat(asDateTimeString(Optional.of(time))).isEqualTo("2005-01-21T07:57:00Z");
  }

  @Test
  public void asIntegerReturnsNullWhenBigIntIsNull() {
    assertThat(asInteger(null)).isNull();
  }

  @Test
  public void asIntegerReturnsValueWhenBigIntIsNull() {
    assertThat(asInteger(BigInteger.TEN)).isEqualTo(10);
  }

  @Test
  public void asReferenceReturnsNullWhenOptionalRefHasDisplayAndTypeAndReference() {
    DatamartReference ref = DatamartReference.of().display("d").type("t").reference("r").build();
    assertThat(asReference(Optional.of(ref)))
        .isEqualTo(Reference.builder().display("d").reference("t/r").build());
  }

  @Test
  public void asReferenceReturnsNullWhenOptionalRefIsNull() {
    assertThat(asReference((Optional<DatamartReference>) null)).isNull();
  }

  @Test
  public void asReferenceReturnsNullWhenRefHasDisplay() {
    DatamartReference ref = DatamartReference.of().display("d").build();
    assertThat(asReference(ref)).isEqualTo(Reference.builder().display("d").build());
  }

  @Test
  public void asReferenceReturnsNullWhenRefHasDisplayAndTypeAndReference() {
    DatamartReference ref = DatamartReference.of().display("d").type("t").reference("r").build();
    assertThat(asReference(ref))
        .isEqualTo(Reference.builder().display("d").reference("t/r").build());
  }

  @Test
  public void asReferenceReturnsNullWhenRefHasTypeAndReference() {
    DatamartReference ref = DatamartReference.of().type("t").reference("r").build();
    assertThat(asReference(ref)).isEqualTo(Reference.builder().reference("t/r").build());
  }

  @Test
  public void asReferenceReturnsNullWhenRefIsEmpty() {
    DatamartReference ref = DatamartReference.of().build();
    assertThat(asReference(ref)).isNull();
  }

  @Test
  public void asReferenceReturnsNullWhenRefIsNull() {
    assertThat(asReference((DatamartReference) null)).isNull();
  }

  @Test
  public void convertAllReturnsConvertedWhenListIsPopulated() {
    assertThat(convertAll(Arrays.asList(1, 2, 3), o -> "x" + o))
        .isEqualTo(Arrays.asList("x1", "x2", "x3"));
  }

  @Test
  public void convertAllReturnsNullWhenListConvertsToToNull() {
    assertThat(convertAll(Arrays.asList(1, 2, 3), o -> null)).isNull();
  }

  @Test
  public void convertAllReturnsNullWhenListIsEmpty() {
    assertThat(convertAll(Collections.emptyList(), o -> "x" + o)).isNull();
  }

  @Test
  public void convertAllReturnsNullWhenListIsNull() {
    assertThat(convertAll(null, o -> "x" + o)).isNull();
  }

  @Test
  public void convertReturnsConvertedWhenItemIsPopulated() {
    Function<Integer, String> tx = o -> "x" + o;
    assertThat(convert(1, tx)).isEqualTo("x1");
  }

  @Test
  public void convertReturnsNullWhenItemIsNull() {
    Function<String, String> tx = o -> "x" + o;
    assertThat(convert(null, tx)).isNull();
  }

  @Test
  public void emptyToNullReturnsNonNullEntries() {
    assertThat(emptyToNull(Arrays.asList("a", null, "b", null))).isEqualTo(List.of("a", "b"));
  }

  @Test
  public void emptyToNullReturnsNullIfEmpty() {
    assertThat(emptyToNull(List.of())).isNull();
  }

  @Test
  public void emptyToNullReturnsNullIfNull() {
    assertThat(emptyToNull(null)).isNull();
  }

  @Test
  public void firstPayloadItemReturnsFirstItemInListWhenPresent() {
    assertThat(firstPayloadItem(Arrays.asList("a", "b"))).isEqualTo("a");
  }

  @Test(expected = MissingPayload.class)
  public void firstPayloadItemThrowsMissingPayloadExceptionWhenEmpty() {
    firstPayloadItem(Collections.emptyList());
  }

  @Test
  public void hasPayloadReturnsPayloadWhenNotNull() {
    assertThat(hasPayload("x")).isEqualTo("x");
  }

  @Test(expected = MissingPayload.class)
  public void hasPayloadThrowsMissingPayloadExceptionWhenNull() {
    hasPayload(null);
  }

  @Test
  public void ifPresentReturnsExtractWhenObjectIsNull() {
    Function<Object, String> extract = (o) -> "x" + o;
    assertThat(ifPresent("a", extract)).isEqualTo("xa");
  }

  @Test
  public void ifPresentReturnsNullWhenObjectIsNull() {
    Function<Object, String> extract = (o) -> "x" + o;
    assertThat(ifPresent(null, extract)).isNull();
  }

  @Test
  public void isBlankCollection() {
    assertThat(isBlank(List.of())).isTrue();
    assertThat(isBlank(List.of("x"))).isFalse();
  }

  @Test
  public void isBlankMap() {
    assertThat(isBlank(Map.of())).isTrue();
    assertThat(isBlank(Map.of("x", "y"))).isFalse();
  }
}
