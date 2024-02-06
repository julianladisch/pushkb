package com.k_int.pushKb.model;

// Generic Error interface we can extend to track errors on Sources/Destinations/PushTasks/Sessions/Chunks?
public interface Error<T> {
  String getCode();
  String getMessage();

  // Must specify an owner -- these errors will ALWAYS hang off an object
  T getOwner();
}
