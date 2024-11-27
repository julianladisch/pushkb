package com.k_int.pushKb.bootstrap;

import java.util.List;

import lombok.Data;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.EachProperty;

@Data
@ConfigurationProperties("destinations")
public class ConfigBootstrapDestinations {
  private List<ConfigBootstrapFolioTenant> foliotenants;
  private List<ConfigBootstrapFolioDestination> foliodestinations;

  @Data
  @EachProperty(value = "foliotenants", list = true)
  public static class ConfigBootstrapFolioTenant {
    private String name;
    private String authtype;
    private String tenant;
    private String baseurl;
    private String user;
    private String password;
  }

  @Data
  @EachProperty(value = "foliodestinations", list = true)
  public static class ConfigBootstrapFolioDestination {
    private String name;
    private String foliotenant;
    private String destinationtype;
  }
}