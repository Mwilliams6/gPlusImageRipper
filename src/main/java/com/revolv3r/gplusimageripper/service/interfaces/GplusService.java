package com.revolv3r.gplusimageripper.service.interfaces;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

public interface GplusService {
  Logger mLogger = LogManager.getLogger(GplusService.class);

  /**
   * Retrieves albumURLS from profile
   * @param userId profile id
   * @return list of string album URLs
   */
  Set<String> retrieveAlbumsFromProfile(String userId);

  /**
   * Retrieve images from album URL
   * @param passedValue album URL
   * @return concatenated image string
   */
  String retrieveImages(String passedValue);

  /**
   * Bypasses the processing of any outstanding jobs
   * @param aState cancellation state
   */
  void setCancelled(boolean aState);
}
