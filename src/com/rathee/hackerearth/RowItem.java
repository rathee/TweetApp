package com.rathee.hackerearth;

public class RowItem {

	private int imageId;
    private String tweetText;
   
 
    public RowItem(int imageId, String tweetText) {
        this.imageId = imageId;
        this.tweetText = tweetText;
        
    }
    public int getImageId() {
        return imageId;
    }
    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
   
   
    public String getTitle() {
        return tweetText;
    }
    public void setTitle(String title) {
        this.tweetText = title;
    }
    @Override
    public String toString() {
        return tweetText + "\n";
    }
}
