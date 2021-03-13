package com.danielkim.soundrecorder.adapters;

public class TagItem {

    // class items
    private int tagId;
    private String tagName;
    private String tagColor;

    // tag constructor
    public TagItem(int tagId, String tagName, String tagColor){

        this.tagId = tagId;
        this.tagName = tagName;
        this.tagColor = tagColor;
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

    public void setTagId(int tagId) {
        this.tagId = tagId;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public void setTagColor(String tagColor) {
        this.tagColor = tagColor;
    }
}
