package com.magicalpipelines.language;

import com.google.cloud.language.v1.AnalyzeEntitySentimentRequest;
import com.google.cloud.language.v1.AnalyzeEntitySentimentResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Document.Type;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.Entity;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.LanguageServiceSettings;
import com.google.cloud.language.v1.Sentiment;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.magicalpipelines.model.EntitySentiment;
import com.magicalpipelines.serialization.Tweet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GcpClient implements LanguageClient {
  private static final Logger log = LoggerFactory.getLogger(GcpClient.class);

  private static ThreadLocal<LanguageServiceClient> nlpClients =
      ThreadLocal.withInitial(
          () -> {
            try {
              LanguageServiceSettings settings = LanguageServiceSettings.newBuilder().build();
              LanguageServiceClient client = LanguageServiceClient.create(settings);
              return client;
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          });

  @Override
  public Tweet translate(Tweet tweet, String targetLanguage) {
    // instantiate a client
    Translate translate = TranslateOptions.getDefaultInstance().getService();

    // translate the tweet text into the target language
    Translation translation =
        translate.translate(
            tweet.getText(),
            TranslateOption.sourceLanguage("en"),
            TranslateOption.targetLanguage(targetLanguage));

    // if you want to get real functional, clone the tweet and set the text on the new object
    tweet.setText(translation.getTranslatedText());
    return tweet;
  }

  @Override
  public List<EntitySentiment> getEntitySentiment(Tweet tweet) {
    List<EntitySentiment> results = new ArrayList<>();

    try {
      Document doc =
          Document.newBuilder().setContent(tweet.getText()).setType(Type.PLAIN_TEXT).build();
      AnalyzeEntitySentimentRequest request =
          AnalyzeEntitySentimentRequest.newBuilder()
              .setDocument(doc)
              .setEncodingType(EncodingType.UTF8)
              .build();
      // Detects the sentiment of the text
      AnalyzeEntitySentimentResponse response = nlpClients.get().analyzeEntitySentiment(request);

      for (Entity entity : response.getEntitiesList()) {

        Sentiment sentiment = entity.getSentiment();

        EntitySentiment entitySentiment =
            EntitySentiment.newBuilder()
                .setCreatedAt(tweet.getCreatedAt())
                .setId(tweet.getId())
                .setEntity(entity.getName().replace("#", "").toLowerCase())
                .setText(tweet.getText())
                .setSalience((double) entity.getSalience())
                .setSentimentScore((double) sentiment.getScore())
                .setSentimentMagnitude((double) sentiment.getMagnitude())
                .build();

        results.add(entitySentiment);
      }
    } catch (Exception e) {
      log.error("Could not detect sentiment for provided text: {}", tweet.getText(), e);
      throw e;
    }
    return results;
  }
}
