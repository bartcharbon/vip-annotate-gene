package org.molgenis.vcf.annotate.gene.exception;

import static java.util.Objects.requireNonNull;

public class UnknownInfoFieldPartException extends RuntimeException {
  private static final String MESSAGE = "The key '%s' is not present in INFO field '%s'";

  public UnknownInfoFieldPartException(String infoField, String key) {
    super(String.format(MESSAGE,requireNonNull(key), requireNonNull(infoField)));
  }
}
