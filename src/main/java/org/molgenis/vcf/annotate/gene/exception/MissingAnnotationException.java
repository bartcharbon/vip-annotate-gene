package org.molgenis.vcf.annotate.gene.exception;

import static java.util.Objects.requireNonNull;

public class MissingAnnotationException extends RuntimeException {
  private static final String MESSAGE = "The input file is missing the required '%s' info field";

  public MissingAnnotationException(String infoField) {
    super(String.format(MESSAGE, requireNonNull(infoField)));
  }
}
