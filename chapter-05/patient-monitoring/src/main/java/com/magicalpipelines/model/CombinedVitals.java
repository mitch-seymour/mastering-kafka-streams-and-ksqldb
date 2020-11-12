package com.magicalpipelines.model;

public class CombinedVitals {
  private final int heartRate;
  private final BodyTemp bodyTemp;

  public CombinedVitals(int heartRate, BodyTemp bodyTemp) {
    this.heartRate = heartRate;
    this.bodyTemp = bodyTemp;
  }

  public int getHeartRate() {
    return this.heartRate;
  }

  public BodyTemp getBodyTemp() {
    return this.bodyTemp;
  }

  @Override
  public String toString() {
    return "{" + " heartRate='" + getHeartRate() + "'" + ", bodyTemp='" + getBodyTemp() + "'" + "}";
  }
}
