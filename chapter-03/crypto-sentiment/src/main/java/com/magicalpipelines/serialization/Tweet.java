package com.magicalpipelines.serialization;

import com.google.gson.annotations.SerializedName;

public class Tweet {
  @SerializedName("CreatedAt")
  private Long createdAt;

  @SerializedName("Id")
  private Long id;

  @SerializedName("Lang")
  private String lang;

  @SerializedName("Retweet")
  private Boolean retweet;

  @SerializedName("Text")
  private String text;

  public Long getCreatedAt() {
    return this.createdAt;
  }

  public void setCreatedAt(Long createdAt) {
    this.createdAt = createdAt;
  }

  public Long getId() {
    return this.id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getLang() {
    return this.lang;
  }

  public void setLang(String lang) {
    this.lang = lang;
  }

  public Boolean isRetweet() {
    return this.retweet;
  }

  public Boolean getRetweet() {
    return this.retweet;
  }

  public void setRetweet(Boolean retweet) {
    this.retweet = retweet;
  }

  public String getText() {
    return this.text;
  }

  public void setText(String text) {
    this.text = text;
  }
}
