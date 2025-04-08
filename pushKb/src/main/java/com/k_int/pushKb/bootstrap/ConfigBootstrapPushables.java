package com.k_int.pushKb.bootstrap;

import java.util.List;

import lombok.Data;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.EachProperty;

@Data
@ConfigurationProperties("pushables")
public class ConfigBootstrapPushables {
  private List<ConfigBootstrapPushTask> pushtasks;

  @Data
  @EachProperty(value = "pushtasks", list = true)
  public static class ConfigBootstrapPushTask {
    private String source;
    private String destination;
  }
}
