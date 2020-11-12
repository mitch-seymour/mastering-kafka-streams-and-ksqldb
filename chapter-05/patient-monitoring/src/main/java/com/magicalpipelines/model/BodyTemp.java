package com.magicalpipelines.model;

public class BodyTemp implements Vital {
  private String timestamp;
  private Double temperature;
  private String unit;

  public String getTimestamp() {
    return this.timestamp;
  }

  public Double getTemperature() {
    return this.temperature;
  }

  public String getUnit() {
    return this.unit;
  }

  @Override
  public String toString() {
    return "{"
        + " timestamp='"
        + getTimestamp()
        + "'"
        + ", temperature='"
        + getTemperature()
        + "'"
        + ", unit='"
        + getUnit()
        + "'"
        + "}";
  }
}
