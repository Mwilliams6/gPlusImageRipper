package com.revolv3r.gplusimageripper.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.revolv3r.gplusimageripper.util.CommonFunctions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service
public class GplusServiceImpl implements GplusService {
  private boolean cancelled = false;
  private static final String GOOGLE_PROFILE_BASE_URL = "http://photos.googleapis.com/data/feed/api/user/";
  private static final String LIGHTBOX_DIV_START = "<a class='lightbox' href='#%s'>";
  private static final String LIGHTBOX_DIV_END = "</a>";
  private static final String LIGHTBOX_TARGET_DIV_START = "<div class='lightbox-target' id='%s'>";
  private static final String LIGHTBOX_CLOSE_BTN = "<a class='lightbox-close' href='#!'>";
  private static final String LIGHTBOX_TARGET_DIV_END = "</div>";
  private static final String LINEBREAK = "<br/>";

  @Override
  public Set<String> retrieveAlbumsFromProfile(String userId)
  {
    Set<String> result = new HashSet<>();
    try
    {
      return getInitialAlbumPages(GOOGLE_PROFILE_BASE_URL+userId);
    }
    catch (IllegalArgumentException e)
    {
      e.printStackTrace();
    }
    return result;
  }

  @Override
  public String retrieveImages(String passedValue)
  {
    if (!cancelled)
    {
      try {
        return getActualAlbumPage(CommonFunctions.correctAlbumUrl(passedValue));
      }catch (IOException e)
      {
        e.printStackTrace();
      }
    }

    return null;
  }

  /**
   * Extract album paths from aPath
   * @param aPath the profile URL
   * @return a set of album path URLs
   */
  private Set<String> getInitialAlbumPages(String aPath)
  {
    try{
      Set<String> albumList = new HashSet<>();
      Document doc = Jsoup.connect(aPath).get();

      Elements matchingDivIds = doc.select("id");

      mLogger.info(String.format("Album: %s, found %s images",
              doc.title(), matchingDivIds.size()));

      for (Element individualImagePath : matchingDivIds) {
        if (individualImagePath.toString().contains("albumid")){
          albumList.add(individualImagePath.toString());
        }
      }
      return albumList;
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Retrieve concatenated images for an album URL
   * @param aAlbumPath the album URL
   * @return concatenated images from profile
   * @throws IOException on error
   */
  private String getActualAlbumPage(String aAlbumPath) throws IOException
  {
    int i =0;
    StringBuilder sb = new StringBuilder();

    Document doc = Jsoup.connect(aAlbumPath).get();
    String newStub = doc.location();
    mLogger.debug(String.format("Parsing Album Title: %s, from URL: %s", doc.title(), aAlbumPath));

    Elements thumbnailPaths = doc.select("img");
    Elements fullsizePaths = doc.select("div.XmeTyb");

    sb.append(String.format("<h4>%s</h4>", doc.title()));
    List<String> fullSizedImages = new ArrayList<>();

    /* TODO: this for loop can probably be streamlined -
     as duplicate paths are returned and dis-regarded, due to how the profile page is structured
     (4 duplicates, 1 unique)  */
    for (Element imagePath : fullsizePaths)
    {
      String uniqueImagePath = CommonFunctions.formatImagePath(imagePath.toString());

      Document paged = Jsoup.connect(newStub + "/" + uniqueImagePath).get();
      Elements matchedFullSizes = paged.select("div.nKtIqb");
      Elements isolatedImagePaths = matchedFullSizes.select("img");
      for (Element single : isolatedImagePaths)
      {
        if (!fullSizedImages.contains(single.toString()))
          fullSizedImages.add(single.toString());
      }
    }

    mLogger.debug(String.format("Found %s thumbnails, and %s full sized images",
            thumbnailPaths.size(), fullsizePaths.size()));

    for (Element thumbImage : thumbnailPaths) {
      String uniqueFileIdent = CommonFunctions.generateUniqueId(thumbImage.toString());
      sb.append(String.format(LIGHTBOX_DIV_START, uniqueFileIdent));
        sb.append(CommonFunctions.replaceAltTag(thumbImage.toString()));
      sb.append(LIGHTBOX_DIV_END);
      sb.append(String.format(LIGHTBOX_TARGET_DIV_START, uniqueFileIdent));
        sb.append(CommonFunctions.replaceAltTag(fullSizedImages.get(i)));
      sb.append(LIGHTBOX_CLOSE_BTN + LIGHTBOX_DIV_END);
      sb.append(LIGHTBOX_TARGET_DIV_END);
      i++;
    }
    sb.append(LINEBREAK);
    return sb.toString();
  }

  /**
   * Allow yet to be completed jobs be skipped
   * @param aState cancellation state
   */
  public void setCancelled(boolean aState)
  {
    cancelled = aState;
  }
}
