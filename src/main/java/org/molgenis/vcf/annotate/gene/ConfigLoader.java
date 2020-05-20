package org.molgenis.vcf.annotate.gene;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import org.molgenis.vcf.annotate.gene.exception.MalformedConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigLoader {
  static Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

  private static final String PREFIX = "#";

  private ConfigLoader() {}

  static Set<AnnotationConfig> loadAnnotationConfig(File annotationConfig) {
    Set<AnnotationConfig> result = new HashSet<>();
    try (Scanner configScanner = new Scanner(annotationConfig)) {
      while (configScanner.hasNextLine()) {
        String line = configScanner.nextLine();
        String[] split = line.split("\t", -1);
        if (line.startsWith(PREFIX)) {
          logger.debug("Skipping line [{}] because it starts with the header prefix '#'", line);
        } else {
          if (split.length == 5) {
            result.add(
                new AnnotationConfig(
                    new File(split[0]), split[1], split[2], toColumns(split[3]), split[4]));
          } else {
            throw new MalformedConfigurationException(line);
          }
        }
      }
    } catch (FileNotFoundException e) {
      throw new IllegalArgumentException(e);
    }
    return result;
  }

  public static List<Column> toColumns(String columns) {
    return Arrays.asList(columns.split(",")).stream()
        .map(
            col -> {
              String[] split = col.split(":");
              if (split.length > 1) {
                return new Column(split[0], Optional.of(split[1]));
              } else {
                return new Column(col, Optional.empty());
              }
            })
        .collect(Collectors.toList());
  }
}
