package com.k_int.pushKb.bootstrap;

import java.util.List;

import lombok.Data;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.EachProperty;

@Data
@ConfigurationProperties("sources")
public class ConfigBootstrapSources {
  private List<ConfigBootstrapGokb> gokbs;
  private List<ConfigBootstrapGokbSource> gokbsources;

  @Data
  @EachProperty(value = "gokbs", list = true)
  public static class ConfigBootstrapGokb {
    private String name;
    private String url;
  }

  @Data
  @EachProperty(value = "gokbsources", list = true)
  public static class ConfigBootstrapGokbSource {
    private String name;
    private String gokb;
    private String type;
  }
}
