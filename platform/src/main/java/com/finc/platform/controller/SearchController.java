package com.finc.platform.controller;

import com.finc.platform.dto.SearchResult;
import com.finc.platform.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "*")
public class SearchController {

    @Autowired
    private SearchService searchService;

    /** GET /api/search?q=meeting */
    @GetMapping
    public SearchResult search(@RequestParam(name = "q", defaultValue = "") String q) {
        return searchService.search(q);
    }
}