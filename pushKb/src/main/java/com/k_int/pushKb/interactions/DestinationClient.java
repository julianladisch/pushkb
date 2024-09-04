package com.k_int.pushKb.interactions;

import com.k_int.pushKb.model.Destination;

// Might need one of these for Source as well?
public interface DestinationClient<T extends Destination> {
  Class<? extends Destination> getDestinationClass();
}
