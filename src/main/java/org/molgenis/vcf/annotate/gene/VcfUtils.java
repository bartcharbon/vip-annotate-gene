package org.molgenis.vcf.annotate.gene;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.util.Strings;
import org.molgenis.vcf.annotate.gene.exception.MissingInfoFieldPartValue;
import org.molgenis.vcf.annotate.gene.exception.UnknownInfoFieldPartException;

public class VcfUtils {

  private static final char SEMICOLON = ':';

  private VcfUtils() {}

  static Set<String> getSubValues(
      String infoField,
      String key,
      String separator,
      VariantContext vc,
      VCFInfoHeaderLine vcfInfoHeaderLine) {
    Set<String> result = new HashSet<>();
    List<String> results = (List<String>) vc.getAttribute(infoField);
    for (String singleVepResult : results) {
      result.add(getValueForKey(infoField, key, separator, vcfInfoHeaderLine, singleVepResult, vc));
    }
    return result;
  }

  static String getValueForKey(
      String infoField,
      String key,
      String separator,
      VCFInfoHeaderLine vcfInfoHeaderLine,
      String singleResult,
      VariantContext vc) {
    int index = getIndex(infoField, key, separator, vcfInfoHeaderLine);
    String[] vepValues = singleResult.split(separator, -1);
    if (vepValues.length >= index) {
      return vepValues[index];
    } else {
      throw new MissingInfoFieldPartValue(infoField, key, vc.getContig() ,vc.getStart(), Strings.join(vc.getAlternateAlleles(),','));
    }
  }

  private static int getIndex(
      String infoField, String key, String separator, VCFInfoHeaderLine vcfInfoHeaderLine) {
    String description = vcfInfoHeaderLine.getDescription();
    String desc = description.substring(description.indexOf(SEMICOLON));
    String[] header = desc.split(separator);
    for (int i = 0; i < header.length; i++) {
      if (header[i].equals(key)) {
        return i;
      }
    }
    throw new UnknownInfoFieldPartException(infoField, key);
  }

  static String createDescription(List<Column> columns, String description, String separator) {
    StringBuilder stringBuilder = new StringBuilder();
    boolean isFirstPass = true;
    if (description != null && !description.isEmpty()) {
      stringBuilder.append(description).append(SEMICOLON);
    }
    for (Column column : columns) {
      if (!isFirstPass) {
        stringBuilder.append(separator);
      }
      isFirstPass = false;
      stringBuilder.append(column.getMapping().orElse(column.getName()));
    }
    return stringBuilder.toString();
  }

  static VCFHeader createAnnotationMeta(
      Set<AnnotationConfig> annotationConfigs, VCFHeader header, String separator) {
    for (AnnotationConfig annotationConfig : annotationConfigs) {
      VCFInfoHeaderLine vcfInfoHeaderLine =
          new VCFInfoHeaderLine(
              annotationConfig.getName(),
              VCFHeaderLineCount.UNBOUNDED,
              VCFHeaderLineType.String,
              createDescription(
                  annotationConfig.getColumns(), annotationConfig.getDescription(), separator));
      header.addMetaDataLine(vcfInfoHeaderLine);
    }
    return header;
  }

  static VariantContextWriter createVCFWriter(final File outFile, VCFHeader header) {
    VariantContextWriterBuilder vcWriterBuilder =
        new VariantContextWriterBuilder().clearOptions().setOutputFile(outFile);
    VariantContextWriter writer = vcWriterBuilder.build();

    writer.writeHeader(header);
    return writer;
  }
}
