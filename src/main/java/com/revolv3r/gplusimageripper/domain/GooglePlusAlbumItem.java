package com.revolv3r.gplusimageripper.domain;

import javax.persistence.*;

@Entity
@Table(name = "gplus_album_data")
public class GooglePlusAlbumItem {

  @Id
  @GeneratedValue(strategy= GenerationType.IDENTITY)
  @Column(name ="pk")
  private int pk;

  @Column(name = "thumb")
  private String mThumbnail;

  @Column(name = "url")
  private String mPageUrl;

  @Column(name = "title")
  private String mTitle;

  public GooglePlusAlbumItem(String aTitle, String aThumb, String aPageUrl)
  {
    mThumbnail = aThumb;
    mPageUrl = aPageUrl;
    mTitle = aTitle;
  }

  public String getThumbnail() {
    return mThumbnail;
  }

  public void setThumbnail(String mThumbnail) {
    this.mThumbnail = mThumbnail;
  }

  public String getPageUrl() {
    return mPageUrl;
  }

  public void setPageUrl(String mPageUrl) {
    this.mPageUrl = mPageUrl;
  }

  public String getTitle() {
    return mTitle;
  }

  public void setTitle(String mTitle) {
    this.mTitle = mTitle;
  }

  public int getPk() {
    return pk;
  }

  public void setPk(int pk) {
    this.pk = pk;
  }
}
