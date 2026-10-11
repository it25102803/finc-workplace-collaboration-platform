package com.finc.platform.dto;

import java.util.ArrayList;
import java.util.List;

public class SearchResult {
    private String query;
    private int totalHits;
    private List<SearchHit> hits = new ArrayList<>();

    public SearchResult(String query) { this.query = query; }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public int getTotalHits() { return totalHits; }
    public void setTotalHits(int totalHits) { this.totalHits = totalHits; }
    public List<SearchHit> getHits() { return hits; }
    public void setHits(List<SearchHit> hits) { this.hits = hits; }

    public void add(SearchHit hit) {
        this.hits.add(hit);
        this.totalHits = this.hits.size();
    }
}