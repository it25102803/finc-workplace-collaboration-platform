package com.example.workplace.controller;

import com.example.workplace.model.KnowledgeItem;
import com.example.workplace.service.CommunicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class KnowledgeController {

    private final CommunicationService communicationService;

    public KnowledgeController(CommunicationService communicationService) {
        this.communicationService = communicationService;
    }

    @GetMapping("/knowledge")
    public List<KnowledgeItem> getAllKnowledge() {
        return communicationService.getAllKnowledge();
    }

    @PostMapping("/knowledge")
    public ResponseEntity<KnowledgeItem> createKnowledge(@RequestBody KnowledgeItem knowledgeItem) {
        try {
            return ResponseEntity.ok(communicationService.createKnowledgeItem(knowledgeItem));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/knowledge/{id}")
    public ResponseEntity<Void> deleteKnowledge(@PathVariable Long id) {
        communicationService.deleteKnowledgeItem(id);
        return ResponseEntity.noContent().build();
    }
}
