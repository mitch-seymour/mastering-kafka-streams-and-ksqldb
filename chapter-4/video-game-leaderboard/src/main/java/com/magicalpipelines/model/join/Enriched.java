package com.magicalpipelines.model.join;

import com.magicalpipelines.model.Product;

public class Enriched implements Comparable<Enriched> {
  private Long playerId;
  private Long productId;
  private String playerName;
  private String gameName;
  private Double score;

  public Enriched(ScoreWithPlayer gameEventWithPlayer, Product product) {
    this.playerId = gameEventWithPlayer.getPlayer().getId();
    this.productId = product.getId();
    this.playerName = gameEventWithPlayer.getPlayer().getName();
    this.gameName = product.getName();
    this.score = gameEventWithPlayer.getScoreEvent().getScore();
  }

  public int compareTo(Enriched o) {
    return Double.compare(o.score, score);
  }

  public Long getPlayerId() {
    return this.playerId;
  }

  public Long getProductId() {
    return this.productId;
  }

  public String getPlayerName() {
    return this.playerName;
  }

  public String getGameName() {
    return this.gameName;
  }

  public Double getScore() {
    return this.score;
  }

  @Override
  public String toString() {
    return "{"
        + " playerId='"
        + getPlayerId()
        + "'"
        + ", playerName='"
        + getPlayerName()
        + "'"
        + ", gameName='"
        + getGameName()
        + "'"
        + ", score='"
        + getScore()
        + "'"
        + "}";
  }
}
