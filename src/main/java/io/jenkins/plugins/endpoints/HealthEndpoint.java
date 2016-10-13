package io.jenkins.plugins.endpoints;

import io.jenkins.plugins.commons.ModelVersionGenerator;
import io.jenkins.plugins.models.ModelVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Endpoint for checking health of the application</p>
 *
 * <p>Used primarily for verifying the model version. The CI job that generates the plugins data
 * will make this call to see what the running application has and compare with itself.</p>
 */
@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
public class HealthEndpoint {

  private Logger logger = LoggerFactory.getLogger(HealthEndpoint.class);

  @GET
  @Path("/model")
  public ModelVersion getModelVersion() {
    try {
      final String version = ModelVersionGenerator.generateModelVersion();
      return new ModelVersion(version);
    } catch (Exception e) {
      logger.error("Problem generating model version", e);
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

}
