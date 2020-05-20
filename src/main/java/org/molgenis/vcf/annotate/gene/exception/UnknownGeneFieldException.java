package org.molgenis.vcf.annotate.gene.exception;

import static java.util.Objects.requireNonNull;

public class UnknownGeneFieldException extends RuntimeException {
  private static final String MESSAGE = "The specified gene field '%s' does not exist in the resource '%s'";

  public UnknownGeneFieldException(String field, String file) {
    super(String.format(MESSAGE, requireNonNull(field), requireNonNull(file)));
  }
}
