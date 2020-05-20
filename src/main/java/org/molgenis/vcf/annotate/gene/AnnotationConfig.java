package org.molgenis.vcf.annotate.gene;

import java.io.File;
import java.util.List;
import lombok.Data;

@Data
public class AnnotationConfig {
  private final File input;
  private final String name;
  private final String description;
  private final List<Column> columns;
  private final String geneColumn;
}
