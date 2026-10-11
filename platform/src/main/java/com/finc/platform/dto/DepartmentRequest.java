package com.finc.platform.dto;

public class DepartmentRequest {
    private String name;
    private String description;
    private Long storageLimitMb;
    private Long managerUserId;   // optional

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getStorageLimitMb() { return storageLimitMb; }
    public void setStorageLimitMb(Long storageLimitMb) { this.storageLimitMb = storageLimitMb; }
    public Long getManagerUserId() { return managerUserId; }
    public void setManagerUserId(Long managerUserId) { this.managerUserId = managerUserId; }
}