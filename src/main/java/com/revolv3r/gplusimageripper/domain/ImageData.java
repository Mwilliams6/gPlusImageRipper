package com.revolv3r.gplusimageripper.domain;

import java.util.ArrayList;
import java.util.List;

public class ImageData {
  private List<String> mFullSizePath = new ArrayList<>();
  private List<String> mFullSizeWithContainingDiv = new ArrayList<>();

  public ImageData(String aFullSizeWithContainingDiv, String aFullSizePath) {
    mFullSizePath.add(aFullSizePath);
    mFullSizeWithContainingDiv.add(aFullSizeWithContainingDiv);
  }

  public boolean doesContainPath(String aCompareVal)
  {
    return mFullSizePath.contains(aCompareVal);
  }

  public List<String> getFullSizePath() {
    return mFullSizePath;
  }

  public void setFullSizePath(String aFullSizePath) {
    mFullSizePath.add(aFullSizePath);
  }

  public List<String> getFullSizeWithContainingDiv() {
    return mFullSizeWithContainingDiv;
  }

  public void setFullSizeWithContainingDiv(String aFullSizeWithContainingDiv) {
    mFullSizeWithContainingDiv.add(aFullSizeWithContainingDiv);
  }
}
