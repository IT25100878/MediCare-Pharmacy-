package com.pharmacy.management.dto;


public class InventoryAlert {

    private final String level;
    private final String title;
    private final String detail;

    public InventoryAlert(String level, String title, String detail) {
        this.level = level;
        this.title = title;
        this.detail = detail;
    }

    public String getLevel() { return level; }
    public String getTitle() { return title; }
    public String getDetail() { return detail; }
}
