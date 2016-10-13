package io.jenkins.plugins.services;

import io.jenkins.plugins.commons.JsonObjectMapper;
import io.jenkins.plugins.models.GeneratedPluginData;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Optional;

/**
 * <p>Mocked ConfigurationService</p>
 */
public class MockConfigurationService implements ConfigurationService {

  private final Logger logger = LoggerFactory.getLogger(MockConfigurationService.class);

  @Override
  public Optional<GeneratedPluginData> getPluginData() throws ServiceException {
    try {
      logger.info("Using test plugin data");
      final ClassLoader cl = getClass().getClassLoader();
      final File dataFile = new File(cl.getResource("plugins.json").getFile());
      final String data = FileUtils.readFileToString(dataFile, "utf-8");
      final GeneratedPluginData generated = JsonObjectMapper.getObjectMapper().readValue(data, GeneratedPluginData.class);
      return Optional.of(generated);
    } catch (Exception e) {
      throw new RuntimeException("Can't get test plugin data");
    }
  }
}
