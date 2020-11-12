package com.magicalpipelines.language;

import com.magicalpipelines.model.EntitySentiment;
import com.magicalpipelines.serialization.Tweet;
import java.util.List;

public interface LanguageClient {
  public Tweet translate(Tweet tweet, String targetLanguage);

  public List<EntitySentiment> getEntitySentiment(Tweet tweet);
}
