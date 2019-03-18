package com.revolv3r.gplusimageripper.domain;

import org.hibernate.annotations.Type;

import javax.persistence.*;

@Entity
@Table(name = "gplus_photo_data")
public class GooglePlusPhoto {

  @Id
  @GeneratedValue(strategy= GenerationType.IDENTITY)
  @Column(name ="pk")
  private int pk;

  @Column(name = "thumb")
  private String mThumbnail;

  @Column(name="fullimage")
  private byte[] mFullImage;

  @Column(name = "url")
  private String mPageUrl;

  @Column(name = "title")
  private String mTitle;

  @Column(name="album_fk")
  private int mAlbum;

  public GooglePlusPhoto(String aTitle, String aThumb, String aPageUrl)
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

  public byte[] getmFullImage() {
    return mFullImage;
  }

  public void setmFullImage(byte[] mFullImage) {
    this.mFullImage = mFullImage;
  }

  public int getmAlbum() {
    return mAlbum;
  }

  public void setmAlbum(int mAlbum) {
    this.mAlbum = mAlbum;
  }
}
