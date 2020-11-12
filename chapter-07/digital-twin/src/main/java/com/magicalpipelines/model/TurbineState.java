package com.magicalpipelines.model;

public class TurbineState {
  private String timestamp;
  private Double windSpeedMph;

  public enum Power {
    ON,
    OFF
  }

  public enum Type {
    DESIRED,
    REPORTED
  }

  private Power power;
  private Type type;

  public TurbineState(String timestamp, Double windSpeedMph, Power power, Type type) {
    this.timestamp = timestamp;
    this.windSpeedMph = windSpeedMph;
    this.power = power;
    this.type = type;
  }

  public static TurbineState clone(TurbineState original) {
    TurbineState clone =
        new TurbineState(
            original.getTimestamp(),
            original.getWindSpeedMph(),
            original.getPower(),
            original.getType());
    return clone;
  }

  public String getTimestamp() {
    return this.timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public Double getWindSpeedMph() {
    return this.windSpeedMph;
  }

  public void setWindSpeedMph(Double windSpeedMph) {
    this.windSpeedMph = windSpeedMph;
  }

  public Power getPower() {
    return this.power;
  }

  public void setPower(Power power) {
    this.power = power;
  }

  public Type getType() {
    return this.type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return "{"
        + " timestamp='"
        + getTimestamp()
        + "'"
        + ", windSpeedMph='"
        + getWindSpeedMph()
        + "'"
        + ", power='"
        + getPower()
        + "'"
        + "}";
  }
}
