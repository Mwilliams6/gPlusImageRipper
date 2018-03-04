package com.revolv3r.gplusimageripper.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
      mLogger.info("Fetched album.. getting next");
      return getActualAlbumPage(passedValue);
    }catch (IOException e)
    {
      e.printStackTrace();
    }
    return null;
  }

//	private void validateInput(String url) throws IllegalArgumentException
//	{
//		if (url.isEmpty())
//			throw new IllegalArgumentException("URL cannot be empty");
//		if (!url.contains("https://get.google.com/u/0/albumarchive/"))
//			throw  new IllegalArgumentException("URL must begin: https://get.google.com/u/0/albumarchive/...");
//	}

  private List<String> getInitialAlbumPages(String aPath)
  {
    try{
      return parseXmlPath(aPath);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }


  private List<String> parseXmlPath(String aUrl) throws Exception{
    List<String> albumList = new ArrayList<>();

    Document doc = Jsoup.connect(aUrl).get();

    mLogger.info(doc.title());

    Elements matchingDivIds = doc.select("id");

    mLogger.info(String.format("found %s images",
            matchingDivIds.size()));

    for (Element headline : matchingDivIds) {
      if (headline.toString().contains("albumid")){

        albumList.add(headline.toString());
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
    StringBuilder sb = new StringBuilder();

    Document doc = Jsoup.connect(aPath).get();

    mLogger.info("Title: " + doc.title());

    Elements matchingDivIds = doc.select("img");
    mLogger.info(String.format("Returned %s images...",matchingDivIds.size()));
    for (Element headline : matchingDivIds) {

      sb.append(headline.toString());
    }

    return sb.toString();
  }
}
