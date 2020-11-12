package com.magicalpipelines.model;

public class ScoreEvent {
  private Long playerId;
  private Long productId;
  private Double score;

  public Long getPlayerId() {
    return this.playerId;
  }

  public void setPlayerId(Long playerId) {
    this.playerId = playerId;
  }

  public Long getProductId() {
    return this.productId;
  }

  public void setProductId(Long productId) {
    this.productId = productId;
  }

  public Double getScore() {
    return this.score;
  }

  public void setScore(Double score) {
    this.score = score;
  }
}
