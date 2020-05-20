package org.molgenis.vcf.annotate.gene;

import static org.molgenis.vcf.annotate.gene.VcfUtils.createAnnotationMeta;
import static org.molgenis.vcf.annotate.gene.VcfUtils.createVCFWriter;

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.molgenis.vcf.annotate.gene.exception.MissingAnnotationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeneAnnotator {
  private static final Logger logger = LoggerFactory.getLogger(GeneAnnotator.class);

  private static final String SEPARATOR = "|";
  private static final String ESC_SEPARATOR = "\\"+SEPARATOR;
  private static final String INFO_FIELD = "CSQ";
  private static final String GENE_KEY = "SYMBOL";

    public void run(File inputFile, File outputFile, File configFile) {
      logger.info("Starting to annotate file!");
      int i = 0;
      Set<AnnotationConfig> configs = ConfigLoader.loadAnnotationConfig(configFile);
      Map<AnnotationConfig, Map<String, String>> annotationResources = new HashMap<>();
      for (AnnotationConfig config : configs) {
        annotationResources.put(config, ResourceLoader.preprocessResource(config));
      }
      try (VCFFileReader reader = new VCFFileReader(inputFile, false)) {
        VCFHeader header = reader.getFileHeader();
        if(header.getInfoHeaderLine(INFO_FIELD) == null){
          throw new MissingAnnotationException(INFO_FIELD);
        }
        header = createAnnotationMeta(annotationResources.keySet(), header, SEPARATOR);
        VariantContextWriter writer = createVCFWriter(outputFile, header);
        for (CloseableIterator<VariantContext> it = reader.iterator(); it.hasNext(); ) {
          VariantContext context = it.next();
          for (Entry<AnnotationConfig, Map<String, String>> entry :
              annotationResources.entrySet()) {
            context =
                annotate(
                    context,
                    entry.getValue(),
                    entry.getKey().getName(),
                    header.getInfoHeaderLine(INFO_FIELD));
          }
          writer.add(context);
          i++;
          if(i % 1000 == 0){
            logger.info("Annotated {} lines.", i);
          }
        }
        writer.close();
    }
    logger.info("Finished: {} lines have been annotated.", i);

  }

  public VariantContext annotate(
      VariantContext vc, Map<String, String> annotations, String name, VCFInfoHeaderLine info) {
    VariantContextBuilder variantContextBuilder = new VariantContextBuilder(vc);
    Set<String> genes = VcfUtils.getSubValues(INFO_FIELD, GENE_KEY, ESC_SEPARATOR, vc, info);
    List<String> results = new ArrayList<>();
    for (String gene : genes) {
      String hit = annotations.get(gene);
      if (hit != null) {
        results.add(hit);
      } else {
        logger.debug("ERROR: gene [{}] was not found in the [{}] annotation resource file", gene, name);
      }
    }
    if (!results.isEmpty()) {
      vc = variantContextBuilder.attribute(name, String.join(",", results)).make();
    }
    return vc;
  }
}
