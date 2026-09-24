package com.finc.platform.dto;

public record CreateFolderRequest(String name, Long ownerId, Long departmentId) {
}
