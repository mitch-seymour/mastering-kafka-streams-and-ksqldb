package com.magicalpipelines;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MyTopologyTest {
  @Test
  public void testDecodeId() {
    String key = "1XRZTUW3";
    byte[] value = new byte[] {};
    String actualValue = MyTopology.decodeKey(key, value);
    String expectedValue = "decoded-1XRZTUW3";
    assertThat(actualValue).isEqualTo(expectedValue);
  }
}
