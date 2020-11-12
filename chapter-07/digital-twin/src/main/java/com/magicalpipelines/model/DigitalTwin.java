package com.magicalpipelines.model;

public class DigitalTwin {
  private TurbineState desired;
  private TurbineState reported;

  public TurbineState getDesired() {
    return this.desired;
  }

  public void setDesired(TurbineState desired) {
    this.desired = desired;
  }

  public TurbineState getReported() {
    return this.reported;
  }

  public void setReported(TurbineState reported) {
    this.reported = reported;
  }

  @Override
  public String toString() {
    return "{" + " desired='" + getDesired() + "'" + ", reported='" + getReported() + "'" + "}";
  }
}
