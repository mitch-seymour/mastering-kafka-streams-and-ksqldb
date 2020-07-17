package com.magicalpipelines;

import com.magicalpipelines.model.join.Enriched;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

public class HighScores {
  private final TreeSet<Enriched> highScores = new TreeSet<>();

  public HighScores add(final Enriched enriched) {
    highScores.add(enriched);

    // keep only the top 3 high scores
    if (highScores.size() > 3) {
      highScores.remove(highScores.last());
    }

    return this;
  }

  public List<Enriched> toList() {

    Iterator<Enriched> scores = highScores.iterator();
    List<Enriched> playerScores = new ArrayList<>();
    while (scores.hasNext()) {
      playerScores.add(scores.next());
    }

    return playerScores;
  }
}
