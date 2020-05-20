package org.molgenis.vcf.annotate.gene;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.stream.Collectors;
import org.molgenis.vcf.annotate.gene.exception.UnknownGeneFieldException;

public class ResourceLoader {

  private static final String SEPARATOR = "|";

  private ResourceLoader() {}

  public static Map<String, String> preprocessResource(AnnotationConfig config) {
    Map<String, String> result = new HashMap<>();
    try (Scanner inputScanner = new Scanner(config.getInput())) {
      Map<String, Integer> headerIndices;
      int geneIndex;
      if (inputScanner.hasNext()) {
        String headerLine = inputScanner.nextLine();
        String[] headers = headerLine.split("\t");
        geneIndex = getGeneIndex(headers, config.getGeneColumn(), config.getInput().getName());
        headerIndices = processHeader(headers, config.getColumns());
      } else {
        throw new IllegalArgumentException("The specified configuration file is empty.");
      }

      while (inputScanner.hasNext()) {
        String data = inputScanner.nextLine();
        String[] values = data.split("\t");
        String line = processLine(headerIndices, values);
        if (!line.isEmpty()) {
          result.put(values[geneIndex], line);
        }
      }
    } catch (FileNotFoundException e) {
      throw new IllegalArgumentException(String.format("Resource file '%s' specified in the configuration file, does not exist.",config.getInput().getName()),e);
    }
    return result;
  }

  private static String processLine(Map<String, Integer> headerIndices, String[] values) {
    StringBuilder geneResultBuilder = new StringBuilder();
    boolean isFirstColumn = true;
    for (Entry<String, Integer> entry : headerIndices.entrySet()) {
      if (!isFirstColumn) {
        geneResultBuilder.append(SEPARATOR);
      }
      isFirstColumn = false;
      String value = values[entry.getValue()];
      geneResultBuilder.append(processValue(value));
    }
    return geneResultBuilder.toString();
  }

  private static String processValue(String value) {
      return value.trim()
          .replace(":", "_")
          .replace(",", "_")
          .replaceAll("\\s", "_");
  }

  private static Map<String, Integer> processHeader(String[] values, List<Column> columns) {
    Map<String, Integer> headerIndices = new HashMap<>();
    List<String> columnNames = columns.stream().map(Column::getName).collect(Collectors.toList());
    int index = 0;
    for (String value : values) {
      if (columnNames.contains(value)) {
        headerIndices.put(value, index);
      }
      index++;
    }
    return headerIndices;
  }

  private static int getGeneIndex(String[] values, String gene, String file) {
    int index = 0;
    for (String value : values) {
      if (gene.equals(value)) {
        return index;
      }
      index++;
    }
    throw new UnknownGeneFieldException(gene, file);
  }
}
