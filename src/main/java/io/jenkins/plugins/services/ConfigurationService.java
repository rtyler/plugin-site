package io.jenkins.plugins.services;

import io.jenkins.plugins.models.GeneratedPluginData;
import org.apache.commons.cli.Option;

import java.util.Optional;

/**
 * <p>Get various configuration pieces for the application</p>
 */
public interface ConfigurationService {

  /**
   * <p>Get plugin data needed to populate Elasticsearch</p>
   *
   * @return GeneratedPluginData - Present if new plugin data is ready, empty if it hasn't changed
   * @throws ServiceException in case something goes wrong
     */
  Optional<GeneratedPluginData> getPluginData() throws ServiceException;

}
