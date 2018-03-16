package com.revolv3r.gplusimageripper.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service
public class GplusServiceImpl implements GplusService {


  private static final String GOOGLE_PROFILE_BASE_URL = "http://photos.googleapis.com/data/feed/api/user/";
  private Logger mLogger = LogManager.getLogger(GplusServiceImpl.class);

  @Override
  public List<String> retrieveAlbumsFromProfile(String userId)
  {
    List<String> result = new ArrayList<>();
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
    try {
      passedValue = correctAlbumUrl(passedValue);
      return getActualAlbumPage(passedValue);
    }catch (IOException e)
    {
      e.printStackTrace();
    }
    return null;
  }

  private List<String> getInitialAlbumPages(String aPath)
  {
    try{
      List<String> results = parseXmlPath(aPath);

      Set<String> hs = new HashSet<>();
      hs.addAll(results);
      results.clear();
      results.addAll(hs);

      return results;
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private List<String> parseXmlPath(String aUrl) throws Exception{
    List<String> albumList = new ArrayList<>();
    Set<String> uniqueImageSet = new HashSet<>();
    Document doc = Jsoup.connect(aUrl).get();

    mLogger.info(doc.title());

    Elements matchingDivIds = doc.select("id");

    mLogger.info(String.format("found %s images",
            matchingDivIds.size()));

    for (Element individualImagePath : matchingDivIds) {
      if (individualImagePath.toString().contains("albumid") && !uniqueImageSet.contains(individualImagePath)){
        albumList.add(individualImagePath.toString());
      }
    }
    return albumList;
  }

  private String correctAlbumUrl(String album) {

    String grabbedURl = album;

    grabbedURl = grabbedURl.replace("albumid", "album");
    grabbedURl = grabbedURl.substring(0, grabbedURl.length()-6);
    grabbedURl = grabbedURl.substring(55, grabbedURl.length());
    return "https://get.google.com/albumarchive/pwaf/" + grabbedURl;
  }

  private String getActualAlbumPage(String aPath) throws IOException
  {
    int i =0;
    StringBuilder sb = new StringBuilder();

    Document doc = Jsoup.connect(aPath).get();
    String newStub = doc.location();
    mLogger.info("Title: " + doc.title());

    Elements matchingDivIds = doc.select("img");
    Elements fullSizeImages = doc.select("div.XmeTyb");
    mLogger.info(String.format("Returned %s images...",matchingDivIds.size()));
    sb.append("<h4>"+ doc.title() +"</h4>");
    List<String> fullSizedImages = new ArrayList<>();

    //String fullsizePath = fullSizeImages.get(0).toString();

    for (Element e : fullSizeImages)
    {
      String uniqueImagePath = formatImagePath(e.toString());
      String fullPathLarge = newStub + "/" + uniqueImagePath;

      Document paged = Jsoup.connect(fullPathLarge).get();
      Elements matchedFullSizes = paged.select("div.nKtIqb");
      Elements paffs = matchedFullSizes.select("img");
      for (Element single : paffs)
      {
        if (!fullSizedImages.contains(single.toString()))
          fullSizedImages.add(single.toString());
      }
    }
    mLogger.info(String.format("Found %s full size images, and %s thumbnails", fullSizedImages.size(), matchingDivIds.size()));
    for (Element headline : matchingDivIds) {
      String uniqueFileIdent = headline.toString().substring(46,56);
      sb.append("<a class='lightbox' href='#"+uniqueFileIdent+"'>");
      sb.append(headline.toString());
      sb.append("</a>");
      sb.append("<div class='lightbox-target' id='"+uniqueFileIdent+"'>");
      sb.append(fullSizedImages.get(i));
      sb.append("<a class='lightbox-close' href='#'></a>");
      sb.append("</div>");
      i++;
    }
    sb.append("<br/>");
    return sb.toString();
  }

  private String formatImagePath(String fullsizePath) {
    String returnStr;
    Integer firstMarker = fullsizePath.indexOf("data-mk");
    returnStr = fullsizePath.substring(firstMarker+9);

    Integer secondMarker = returnStr.indexOf("jsdata");
    returnStr = returnStr.substring(0, secondMarker-2);

    return returnStr;
  }
}
