package com.revolv3r.gplusimageripper.service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.revolv3r.gplusimageripper.domain.ImageData;
import com.revolv3r.gplusimageripper.service.interfaces.GplusService;
import com.revolv3r.gplusimageripper.util.CommonFunctions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

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

  private FileOutputStream fos;


  public GplusServiceImpl() {
    fos = null;
  }

  private ZipOutputStream getZos()
  {
    if (fos == null)
    {
      try
      {
        fos = new FileOutputStream("hello-world.zip");
      }
      catch (FileNotFoundException fNfe)
      {
        //TODO: do something miraculous
      }
    }
    BufferedOutputStream bos = new BufferedOutputStream(fos);
    return new ZipOutputStream(bos);
  }

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

  private void makeZippableContent()
  {

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
    ImageData fullSizedImages = new ImageData();

    /* TODO: this for loop can probably be streamlined, but exacly how, escapes me for now -
     as duplicate paths are returned and thrown away, due to how a profile page is structured
     (4 duplicates, 1 unique)  */
    for (Element imagePath : fullsizePaths)
    {
      String uniqueImagePath = CommonFunctions.formatImagePath(imagePath.toString());

      Document paged = Jsoup.connect(newStub + "/" + uniqueImagePath).get();
      Elements matchedFullSizes = paged.select("div.nKtIqb");
      Elements isolatedImagePaths = matchedFullSizes.select("img");
      for (Element single : isolatedImagePaths)
      {
        String fullsizedPath = single.absUrl("src");
        if (!fullSizedImages.doesContainPath(fullsizedPath))
        {
          fullSizedImages.setFullSizePath(fullsizedPath);
          fullSizedImages.setFullSizeWithContainingDiv(single.toString());
        }
      }
    }

    mLogger.debug(String.format("Found %s thumbnails, and %s full sized images",
            thumbnailPaths.size(), fullsizePaths.size()));

    for (Element thumbImage : thumbnailPaths) {
      String thumbImageWithContainerDiv = CommonFunctions.replaceAltTag(thumbImage.toString());
      String thumbImagePath = thumbImage.absUrl("src");
      String uniqueFileIdent = CommonFunctions.generateUniqueId(thumbImage.toString());
      sb.append(String.format(LIGHTBOX_DIV_START, uniqueFileIdent));
        sb.append(thumbImageWithContainerDiv);
      sb.append(LIGHTBOX_DIV_END);
      sb.append(String.format(LIGHTBOX_TARGET_DIV_START, uniqueFileIdent));
        sb.append(CommonFunctions.replaceAltTag(fullSizedImages.getFullSizeWithContainingDiv().get(i)));
      sb.append(LIGHTBOX_CLOSE_BTN + LIGHTBOX_DIV_END);
      sb.append(LIGHTBOX_TARGET_DIV_END);

      buildResourceWithUrls(
              thumbImagePath,
              fullSizedImages.getFullSizePath().get(i),
              uniqueFileIdent);

      i++;
    }
    sb.append(LINEBREAK);
    return sb.toString();
  }

  private void buildResourceWithUrls(String aThumb, String aFullSized, String aCleansedFilename)
  {
    InputStream thumbnail=null, fullsize=null;
    try
    {
      thumbnail = new URL(""+aThumb).openStream();
      fullsize = new URL(""+aFullSized).openStream();

      addResourcesToReturnObject(thumbnail, fullsize, aCleansedFilename);
    }
    catch (MalformedURLException mE)
    {
      //TODO: do something
    }catch (IOException iE)
    {
      //TODO: do something else
    }
    finally
    {
      try
      {
        if (thumbnail!=null)
          thumbnail.close();

        if (fullsize!=null)
          fullsize.close();
      }
      catch (IOException ie)
      {
        //TODO: do special stuffs
      }
    }
  }

  private void addResourcesToReturnObject(InputStream aThumbnail, InputStream aFullsize, String aFilename) {
    BufferedInputStream thumbIs = new BufferedInputStream(aThumbnail);
    BufferedInputStream fullsizeIs = new BufferedInputStream(aFullsize);

    //byte[] buf = new byte[1024];
    try
    {
      BufferedImage thumbnailImage = ImageIO.read(thumbIs);
      BufferedImage fullsizeImage = ImageIO.read(fullsizeIs);

      //FileOutputStream outputStream = new FileOutputStream("output.zip");
      ZipOutputStream zipOutputStream = new ZipOutputStream(getZos());
      ZipEntry imageZipThumbOutput = new ZipEntry("/album"+aFilename+"/thumbs/" + aFilename + ".png");
      ZipEntry imageZipFullOutput = new ZipEntry("/album"+aFilename+"/" + aFilename + "_full.png");

      zipOutputStream.putNextEntry(imageZipThumbOutput);
      ImageIO.write(thumbnailImage,imageZipThumbOutput.getName(), getZos());

      zipOutputStream.putNextEntry(imageZipFullOutput);
      ImageIO.write(fullsizeImage,imageZipFullOutput.getName(), getZos());
//      }

      thumbIs.close();
      zipOutputStream.closeEntry();

      zipOutputStream.close();

    }
    catch (IOException e)
    {
      //TODO: do something
      e.printStackTrace();
    }

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
