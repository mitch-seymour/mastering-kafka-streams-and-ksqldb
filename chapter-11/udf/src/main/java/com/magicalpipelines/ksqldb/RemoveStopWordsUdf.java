package com.magicalpipelines.ksqldb;

import io.confluent.ksql.function.udf.Udf;
import io.confluent.ksql.function.udf.UdfDescription;
import io.confluent.ksql.function.udf.UdfParameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UdfDescription(
    name = "remove_stop_words",
    description = "A UDF that removes stop words from a string of text",
    version = "0.1.0",
    author = "Mitch Seymour")
public class RemoveStopWordsUdf {

  private final List<String> stopWords =
      Arrays.asList(
          // ...
          new String[] {"a", "and", "are", "but", "or", "over", "the"});

  private ArrayList<String> stringToWords(String source) {
    return Stream.of(source.toLowerCase().split(" "))
        .collect(Collectors.toCollection(ArrayList<String>::new));
  }

  private String wordsToString(ArrayList<String> words) {
    return words.stream().collect(Collectors.joining(" "));
  }

  @Udf(description = "Remove the default stop words from a string of text")
  public String apply(
      @UdfParameter(value = "source", description = "the raw source string") final String source) {
    ArrayList<String> words = stringToWords(source);
    words.removeAll(stopWords);
    return wordsToString(words);
  }

  @Udf(description = "Remove the a custom set of stop words from a string of text")
  public String apply(
      @UdfParameter(value = "source", description = "the source string to remove stop words from")
          final String source,
      @UdfParameter(value = "stop_words", description = "the stop words to remove")
          final List<String> stopWords) {
    ArrayList<String> words = stringToWords(source);
    words.removeAll(stopWords);
    return wordsToString(words);
  }
}
