package io.jenkins.plugins.datastore.impl;

import io.jenkins.plugins.datastore.DatastoreException;
import io.jenkins.plugins.datastore.DatastoreService;
import io.jenkins.plugins.datastore.support.ElasticsearchTransformer;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.json.JSONObject;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ElasticsearchDatastoreService implements DatastoreService {

  @Inject
  private Client esClient;

  @Override
  public JSONObject search(String query, String sort, List<String> labels, List<String> authors, String core, Integer size, Integer page) throws DatastoreException {
    try {
      final SearchRequestBuilder requestBuilder = esClient.prepareSearch("plugins")
        .addFields("name", "url", "title", "wiki", "excerpt", "labels", "categories")
        .addHighlightedField("excerpt")
        .setHighlighterFragmentSize(500)
        .setHighlighterNumOfFragments(1)
        .setHighlighterPreTags("<mark>")
        .setHighlighterPostTags("</mark>")
        .setFrom((page - 1) * size)
        .setSize(size);
      final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
        .should(QueryBuilders.matchQuery("title", query))
        .should(QueryBuilders.matchQuery("name", query))
        .must(QueryBuilders.matchQuery("excerpt", query));
      if (!labels.isEmpty()) {
        queryBuilder.should(QueryBuilders.termsQuery("labels", labels));
      }
      final SearchResponse response = requestBuilder.setQuery(queryBuilder).execute().get();
      final long total = response.getHits().getTotalHits();
      final JSONObject result = new JSONObject();
      result.put("docs", ElasticsearchTransformer.transformHits(response.getHits()));
      result.put("total", total);
      result.put("page", page);
      result.put("pages", (total + size - 1) / size);
      return result;
    } catch (InterruptedException | ExecutionException e) {
      throw new DatastoreException("Problem executing ES query", e);
    }
  }

  @Override
  public JSONObject get(String name) {
    try {
      final GetResponse getResponse = esClient.prepareGet("plugins", "plugins", name).execute().get();
      return getResponse.isExists() ? ElasticsearchTransformer.transformGet(getResponse) : null;
    } catch (InterruptedException | ExecutionException e) {
        throw new DatastoreException("Problem executing ES query", e);
    }
  }
}
