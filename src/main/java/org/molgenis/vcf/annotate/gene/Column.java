package org.molgenis.vcf.annotate.gene;

import java.util.Optional;
import lombok.Data;

@Data
public class Column {
  private final String name;
  private final Optional<String> mapping;
}
