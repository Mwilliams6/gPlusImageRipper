package com.revolv3r.gplusimageripper.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.revolv3r.gplusimageripper.domain.GooglePlusAlbumItem;
import com.revolv3r.gplusimageripper.domain.GooglePlusPhoto;
import com.revolv3r.gplusimageripper.util.CommonFunctions;
import com.revolv3r.gplusimageripper.dao.GPlusAlbumItemRepository;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GplusServiceImpl implements GplusService {
  private boolean cancelled = false;
  private static final String GOOGLE_PROFILE_BASE_URL = "http://photos.googleapis.com/data/feed/api/user/";
  private static final String LIGHTBOX_DIV_START = "<a class='lightbox' href='%s'>";
  private static final String LIGHTBOX_DIV_END = "</a>";
  private static final String LIGHTBOX_TARGET_DIV_START = "<div class='lightbox-target' id='%s'>";
  private static final String LIGHTBOX_CLOSE_BTN = "<a class='lightbox-close' href='#!'>";
  private static final String LIGHTBOX_TARGET_DIV_END = "</div>";
  private static final String LINEBREAK = "<br/>";

  @Autowired
  private GPlusAlbumItemRepository mGPlusAlbumItemRepository;

  @Override
  public List<GooglePlusAlbumItem> retrieveAlbumsFromProfile(String userId)
  {
    try
    {
      return getInitialAlbumPages("audreyAlbumHome.html");
    }
    catch (IllegalArgumentException e)
    {
      e.printStackTrace();
    }
    return new ArrayList<>();
  }

  @Override
  public String retrieveImages(String passedValue)
  {
    if (!cancelled)
    {
      try {
        return getActualAlbumPage(passedValue);
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
  private List<GooglePlusAlbumItem> getInitialAlbumPages(String aPath)
  {
    try{
      URL urlPath = this.getClass().getClassLoader().getResource(aPath);
      Document doc = Jsoup.parse(new File(urlPath.toURI()), "ISO-8859-1");

      Elements matchingDivIds = doc.select("div.NzRmxf");

      mLogger.info(String.format("Album: %s, found %s albums",
              doc.title(), matchingDivIds.size()));

      List<GooglePlusAlbumItem> itemList = new ArrayList<>();
      for (Element individualImagePath : matchingDivIds) {
        mLogger.info(individualImagePath.parent().toString());

        String title = individualImagePath.child(0).attr("aria-label");
        String thumbnail = individualImagePath.attr("data-bpu");
        String pageUrl = individualImagePath.attr("data-link");
        GooglePlusAlbumItem item = new GooglePlusAlbumItem(title, thumbnail, pageUrl);
        itemList.add(item);
        mGPlusAlbumItemRepository.save(item);

        //get album images
        Document album = Jsoup.connect("https://get.google.com/"+pageUrl.substring(2)+"?source=pwa").get();
        Elements matchingImgIds = album.select("div.XmeTyb");

        for (Element imagePath : matchingImgIds)
        {

          String photoTitle ="";
          String photoThumbnail ="";
          String thumbsrc = imagePath.child(0).attr("src");

          GooglePlusPhoto photo = new GooglePlusPhoto(photoTitle, thumbsrc, "none");
//
//          File file = new File(thumbsrc);
//          byte[] fullsize = new byte[(int) file.length()];
//          try {
//            FileInputStream fileInputStream = new FileInputStream(file);
//            //convert file into array of bytes
//            fileInputStream.read(fullsize);
//            fileInputStream.close();
//          } catch (Exception e) {
//            e.printStackTrace();
//          }

          //photo.setmFullImage(fullsize);
          photo.setmAlbum(item.getPk());

          mGPlusAlbumItemRepository.save(item);
          mLogger.info("===");
        }
      }
      return itemList;
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
    //String newStub = doc.location();
    mLogger.debug(String.format("Parsing Album Title: %s, from URL: %s", doc.title(), aAlbumPath));

    Elements thumbnailPaths = doc.select("img");

    //get page containing full sized
    Elements fullsizePaths = doc.select("div.XmeTyb");

    List<String> foundFullSizes = new ArrayList<>();

    for (Element e : fullsizePaths)
    {
      String found = e.attr("data-mk"); //fullsize url

      Document fullSizePaged = Jsoup.connect(doc.location()+"/"+found).get();

      Elements largeImagePage = fullSizePaged.select("div.ZRciZe");

      for (Element e1 : largeImagePage)
      {
        Elements pageImageDivs = largeImagePage.select("div.nKtIqb");

        for (Element e4 : pageImageDivs)
        {
          //check for video
          if(e4.toString().contains("Start video"))
          {
            //go digging for url
            String videoUrl = e4.attr("data-dlu");

            if(!foundFullSizes.contains(videoUrl))
              foundFullSizes.add(videoUrl);
          }
          else
          {
            String imageValue = e4.select("img").get(0).toString();//first img in div
            if(!foundFullSizes.contains(imageValue))
              foundFullSizes.add(imageValue);

            mLogger.info("done image grabs");
          }
        }
      }
    }

    //draw results
    sb.append(String.format("<h4>%s</h4>", doc.title()));

    for (Element thumbImage : thumbnailPaths) {
      String uniqueFileIdent = CommonFunctions.generateUniqueId(thumbImage.toString());
      if(foundFullSizes.get(i).startsWith("http"))
      {
        sb.append(String.format(LIGHTBOX_DIV_START, foundFullSizes.get(i)));
          sb.append(CommonFunctions.replaceAltTag(thumbImage.toString()));
        sb.append(LIGHTBOX_DIV_END);
      }
      else
      {
        sb.append(String.format(LIGHTBOX_DIV_START, "#"+uniqueFileIdent));
          sb.append(CommonFunctions.replaceAltTag(thumbImage.toString()));
        sb.append(LIGHTBOX_DIV_END);
        sb.append(String.format(LIGHTBOX_TARGET_DIV_START, uniqueFileIdent));
        sb.append(CommonFunctions.replaceAltTag(foundFullSizes.get(i)));
        sb.append(LIGHTBOX_CLOSE_BTN + LIGHTBOX_DIV_END);
        sb.append(LIGHTBOX_TARGET_DIV_END);
      }

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
