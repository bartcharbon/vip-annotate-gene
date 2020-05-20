package org.molgenis.vcf.annotate.gene.exception;

import static java.util.Objects.requireNonNull;

public class MissingInfoFieldPartValue extends RuntimeException {
  private static final String MESSAGE = "No value for key '%s' in INFO field '%s' for #CHROM '%s', POS '%s', ALT '%s'";

  public MissingInfoFieldPartValue(String infoField, String key, String chrom, int pos, String alt) {
    super(String.format(MESSAGE,requireNonNull(key), requireNonNull(infoField), requireNonNull(chrom), requireNonNull(pos), requireNonNull(alt)));
  }
}
