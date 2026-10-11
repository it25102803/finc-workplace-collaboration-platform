package com.finc.platform.dto;

public class StorageQuotaRequest {
    private Long limitMb;

    public Long getLimitMb() { return limitMb; }
    public void setLimitMb(Long limitMb) { this.limitMb = limitMb; }
}