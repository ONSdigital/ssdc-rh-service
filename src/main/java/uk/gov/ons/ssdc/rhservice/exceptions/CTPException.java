package uk.gov.ons.ssdc.rhservice.exceptions;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
import java.text.SimpleDateFormat;

@JsonSerialize(using = CTPException.OurExceptionSerializer.class)
public class CTPException extends Exception {

  private static final long serialVersionUID = -1569645569528433069L;
  private static final String UNDEFINED_MSG = "Non Specific Error";

  /** The list of CTP faults */
  public enum Fault {
    /** For system errors */
    SYSTEM_ERROR,
    /** For resources not found */
    RESOURCE_NOT_FOUND,
    /** For resource version conflicts */
    RESOURCE_VERSION_CONFLICT,
    /** For when a validation failed */
    VALIDATION_FAILED,
    /** For access denied */
    ACCESS_DENIED,
    /** For bad requests */
    BAD_REQUEST,
    /** 429 error for overloaded service */
    TOO_MANY_REQUESTS,
    /** For operations in progress */
    ACCEPTED_UNABLE_TO_PROCESS;
  }

  private Fault fault;
  private long timestamp = System.currentTimeMillis();

  /**
   * Constructor
   *
   * @param afault associated with the CTPException about to be created.
   */
  public CTPException(final Fault afault) {
    this(afault, UNDEFINED_MSG, (Object[]) null);
  }

  /**
   * Constructor
   *
   * @param afault associated with the CTPException about to be created.
   * @param cause associated with the CTPException about to be created.
   */
  public CTPException(final Fault afault, final Throwable cause) {
    this(afault, cause, (cause != null) ? cause.getMessage() : "", (Object[]) null);
  }

  /**
   * Constructor
   *
   * @param afault associated with the CTPException about to be created.
   * @param message associated with the CTPException about to be created.
   * @param args substitutes for the message string.
   */
  public CTPException(final Fault afault, final String message, final Object... args) {
    this(afault, null, message, args);
  }

  /**
   * Constructor
   *
   * @param afault associated with the CTPException about to be created.
   * @param cause associated with the CTPException about to be created.
   * @param message associated with the CTPException about to be created.
   * @param args substitutes for the message string.
   */
  public CTPException(
      final Fault afault, final Throwable cause, final String message, final Object... args) {
    super((message != null) ? String.format(message, args) : "", cause);
    fault = afault;
  }

  /** @return the fault associated with the CTPException. */
  public final Fault getFault() {
    return fault;
  }

  /** @return the timestamp when the CTPException was created. */
  public final long getTimestamp() {
    return timestamp;
  }

  /** To customise the serialization of CTPException objects */
  public static class OurExceptionSerializer extends JsonSerializer<CTPException> {
    /**
     * To customise the serialization of CTPException objects
     *
     * @param value the CTPException to serialize
     * @param jgen the Jackson JsonGenerator
     * @param provider the Jackson SerializerProvider
     */
    @Override
    public final void serialize(
        final CTPException value, final JsonGenerator jgen, final SerializerProvider provider)
        throws IOException {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");

      jgen.writeStartObject();
      jgen.writeFieldName("error");
      jgen.writeStartObject();
      jgen.writeStringField("code", value.getFault().name());
      jgen.writeStringField("timestamp", sdf.format(value.getTimestamp()));
      jgen.writeStringField("message", value.getMessage());

      jgen.writeEndObject();
      jgen.writeEndObject();
    }
  }

  @Override
  public String toString() {
    return super.toString() + ": " + fault.name();
  }
}
