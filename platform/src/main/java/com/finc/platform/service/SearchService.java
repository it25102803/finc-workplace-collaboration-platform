package com.finc.platform.service;

import com.finc.platform.dto.SearchHit;
import com.finc.platform.dto.SearchResult;
import com.finc.platform.entity.*;
import com.finc.platform.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

    @Autowired private UserRepository userRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private ChannelRepository channelRepository;
    @Autowired private TaskRepository taskRepository;
    @Autowired private FileRepository fileRepository;
    @Autowired private KnowledgeArticleRepository knowledgeArticleRepository;

    public SearchResult search(String q) {
        SearchResult result = new SearchResult(q);
        if (q == null || q.isBlank() || q.length() < 2) return result;

        String query = q.toLowerCase();

        // ---- Users ----
        userRepository.findAll().stream()
                .filter(u -> contains(u.getUsername(), query)
                        || contains(u.getEmail(), query)
                        || contains(u.getFirstName(), query)
                        || contains(u.getLastName(), query))
                .limit(5)
                .forEach(u -> result.add(new SearchHit(
                        "USER", u.getId(),
                        (u.getFirstName() != null ? u.getFirstName() : "") + " " +
                                (u.getLastName() != null ? u.getLastName() : ""),
                        u.getEmail() + " · " + (u.getRole() != null ? u.getRole().getRoleType().name() : "—"),
                        "user-directory.html"
                )));

        // ---- Departments ----
        departmentRepository.findAll().stream()
                .filter(d -> contains(d.getName(), query)
                        || contains(d.getDescription(), query))
                .limit(3)
                .forEach(d -> result.add(new SearchHit(
                        "DEPARTMENT", d.getDepartmentId(),
                        d.getName(),
                        d.getDescription() == null ? "" : d.getDescription(),
                        "admin-settings.html"
                )));

        // ---- Channels ----
        channelRepository.findAll().stream()
                .filter(c -> contains(c.getName(), query)
                        || contains(c.getDescription(), query))
                .limit(5)
                .forEach(c -> result.add(new SearchHit(
                        "CHANNEL", c.getId(),
                        "# " + c.getName(),
                        c.getDescription() == null ? "" : c.getDescription(),
                        "channels.html?channel=" + c.getId()
                )));

        // ---- Tasks ----
        taskRepository.findAll().stream()
                .filter(t -> contains(t.getTitle(), query)
                        || contains(t.getDescription(), query))
                .limit(5)
                .forEach(t -> result.add(new SearchHit(
                        "TASK", t.getTaskId(),
                        t.getTitle(),
                        "Status: " + (t.getStatus() != null ? t.getStatus().name() : "—"),
                        "tasks.html?task=" + t.getTaskId()
                )));

        // ---- Files ----
        fileRepository.findAll().stream()
                .filter(f -> contains(f.getFileName(), query))
                .limit(5)
                .forEach(f -> result.add(new SearchHit(
                        "FILE", f.getFileId(),
                        f.getFileName(),
                        "Type: " + (f.getFileType() != null ? f.getFileType().name() : "—"),
                        "files.html?file=" + f.getFileId()
                )));

        // ---- Knowledge articles ----
        knowledgeArticleRepository.findByStatus(ArticleStatus.PUBLISHED).stream()
                .filter(k -> contains(k.getTitle(), query)
                        || contains(k.getContent(), query)
                        || contains(k.getCategory().getName(), query))
                .limit(5)
                .forEach(k -> result.add(new SearchHit(
                        "ARTICLE", k.getArticleId(),
                        k.getTitle(),
                        "Category: " + k.getCategory().getName(),
                        "knowledge-base.html?article=" + k.getArticleId()
                )));

        return result;
    }

    private boolean contains(String s, String q) {
        return s != null && s.toLowerCase().contains(q);
    }
}