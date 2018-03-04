package com.revolv3r.gplusimageripper.service;

import java.util.List;

public interface GplusService {
  List<String> retrieveAlbumsFromProfile(String userId);
  String retrieveImages(String passedValue);
}
