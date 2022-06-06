package uk.gov.ons.ssdc.rhservice.exceptions;

public class DataStoreContentionException extends Exception {
  private static final long serialVersionUID = 4250385007849932900L;

  public DataStoreContentionException(String message, Exception e) {
    super(message, e);
  }
}
