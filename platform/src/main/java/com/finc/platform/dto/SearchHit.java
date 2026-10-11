package com.finc.platform.dto;

public class SearchHit {
    private String type;    // "USER", "DEPARTMENT", "CHANNEL", "TASK", "FILE", "ARTICLE"
    private Long id;
    private String title;
    private String subtitle;
    private String link;    // URL to open

    public SearchHit() {}
    public SearchHit(String type, Long id, String title, String subtitle, String link) {
        this.type = type;
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.link = link;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
}