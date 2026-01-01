package com.cineworm.item;

public class ItemHomeDisplay {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_CONTENT = 1;

    private int type;
    private String sectionTitle;
    private String sectionId;
    private ItemHomeContent content;

    // Constructor for header
    public ItemHomeDisplay(String sectionTitle, String sectionId) {
        this.type = TYPE_HEADER;
        this.sectionTitle = sectionTitle;
        this.sectionId = sectionId;
    }

    // Constructor for content
    public ItemHomeDisplay(ItemHomeContent content) {
        this.type = TYPE_CONTENT;
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public String getSectionTitle() {
        return sectionTitle;
    }

    public String getSectionId() {
        return sectionId;
    }

    public ItemHomeContent getContent() {
        return content;
    }
}
