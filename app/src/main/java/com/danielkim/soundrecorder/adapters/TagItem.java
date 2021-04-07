package com.danielkim.soundrecorder.adapters;

public class TagItem {

    // class items
    private int tagId;
    private String tagName;
    private String tagColor;
    private String textColor;

    // tag constructor
    public TagItem(int tagId, String tagName, String tagColor, String textColor){

        this.tagId = tagId;
        this.tagName = tagName;
        this.tagColor = tagColor;
        this.textColor = textColor;
    }

    public int getTagId() {
        return tagId;
    }

    public String getTagName() {
        return tagName;
    }

    public String getTagColor() {
        return tagColor;
    }

    public String getTagTextColor(){ return textColor; }

    public void setTagId(int tagId) {
        this.tagId = tagId;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public void setTagColor(String tagColor) {
        this.tagColor = tagColor;
    }

    public void setTextColor(String textColor) { this.textColor = textColor; }
}
