package io.jenkins.plugins.commons;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.elasticsearch.Version;

import java.io.File;

/**
 * <p>Utility for generating the model version</p>
 */
public final class ModelVersion {

  public static String generateModelVersion() {
    try {
      final ClassLoader cl = ModelVersion.class.getClassLoader();
      final File mappingFile = new File(cl.getResource("elasticsearch/mappings/plugins.json").getFile());
      final String mappingContent = FileUtils.readFileToString(mappingFile, "utf-8");
      final String version = Version.CURRENT.toString();
      return DigestUtils.sha256Hex(mappingContent+version);
    } catch (Exception e) {
      throw new RuntimeException("Problem generating model version", e);
    }
  }

}
