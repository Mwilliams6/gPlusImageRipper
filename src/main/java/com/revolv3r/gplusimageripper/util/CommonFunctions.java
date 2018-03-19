package com.revolv3r.gplusimageripper.util;

public class CommonFunctions {
  private static final String ALBUM_ARCHIVE_BASE_URL = "https://get.google.com/albumarchive/pwaf/";
  public final static String PROGRESS_BAR = "<div class='progress'><div class='progress-bar' role='progressbar' style='width:%s%%'></div></div>";

  public static String correctAlbumUrl(String album)
  {
    String grabbedURl = album;
    grabbedURl = grabbedURl.replace("albumid", "album");
    grabbedURl = grabbedURl.substring(0, grabbedURl.length()-6);
    grabbedURl = grabbedURl.substring(55, grabbedURl.length());
    return ALBUM_ARCHIVE_BASE_URL + grabbedURl;
  }

  public static String formatImagePath(String fullsizePath) {
    String returnStr;
    Integer firstMarker = fullsizePath.indexOf("data-mk");
    returnStr = fullsizePath.substring(firstMarker+9);

    Integer secondMarker = returnStr.indexOf("jsdata");
    returnStr = returnStr.substring(0, secondMarker-2);

    return returnStr;
  }

  public static String generateUniqueId(String s) {
    return s.substring(46,56);
  }

  public static String replaceAltTag(String s) {
  return s.replace("alt=", "title=");
  }
}
