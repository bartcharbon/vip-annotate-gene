package org.molgenis.vcf.annotate.gene.exception;

import static java.util.Objects.requireNonNull;

public class MalformedConfigurationException extends RuntimeException {
  private static final String MESSAGE =
      "Invalid config '%s' line should contain inputfile\\tfieldname\\tdescription\\tcolumns\\tgeneColumn";

  public MalformedConfigurationException(String line) {
    super(String.format(MESSAGE, requireNonNull(line)));
  }
}
