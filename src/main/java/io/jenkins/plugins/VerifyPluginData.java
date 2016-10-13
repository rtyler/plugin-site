package io.jenkins.plugins;

import io.jenkins.plugins.commons.JsonObjectMapper;
import io.jenkins.plugins.commons.ModelVersionGenerator;
import io.jenkins.plugins.models.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.jgit.api.Git;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPOutputStream;

/**
 * <p>Class that verifies the final plugins.json.gzip file can be injested by the running API.</p>
 */
public class VerifyPluginData {

  private static final Logger logger = LoggerFactory.getLogger(VerifyPluginData.class);

  public static void main(String[] args) {
    final VerifyPluginData verifyPluginData = new VerifyPluginData();
    verifyPluginData.verify();
  }

  public void verify() {
    if (BooleanUtils.toBoolean(System.getenv().getOrDefault("FORCE", "false"))) {
      logger.info("Forcing plugin data verification");
      return;
    }
    final String url = StringUtils.trimToNull(System.getenv().getOrDefault("REST_API_URL", null));
    if (url == null) {
      logger.error("REST_API_URL is empty or not supplied. Not generating plugin data out of safety.");
      throw new RuntimeException("REST_API_URL is empty or not supplied. Not generating plugin data out of safety.");
    }
    final CloseableHttpClient httpClient = HttpClients.createDefault();
    try {
      final HttpGet get = new HttpGet(String.format("%s/health/model", url));
      final CloseableHttpResponse response = httpClient.execute(get);
      if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        final HttpEntity entity = response.getEntity();
        final InputStream inputStream = entity.getContent();
        final String data = IOUtils.toString(inputStream, Charset.forName("utf-8"));
        final ModelVersion modelVersion = JsonObjectMapper.getObjectMapper().readValue(data, ModelVersion.class);
        if (!modelVersion.getVersion().equalsIgnoreCase(ModelVersionGenerator.generateModelVersion())) {
          logger.error("Model version on API doesn't match generated");
          throw new RuntimeException("Model version on API doesn't match generated");
        }
      } else {
        logger.error(String.format("Unable to communicate with API (%s). Not generating plugin data out of safety.", url));
        throw new RuntimeException(String.format("Unable to communicate with API (%s). Not generating plugin data out of safety.", url));
      }
    } catch (Exception e) {
      logger.error("Problem verifying model versions match", e);
      throw new RuntimeException("Problem verifying model versions match", e);
    } finally {
      try {
        httpClient.close();
      } catch (IOException e) {
        logger.error("Problem closing httpClient", e);
      }
    }
  }

}
