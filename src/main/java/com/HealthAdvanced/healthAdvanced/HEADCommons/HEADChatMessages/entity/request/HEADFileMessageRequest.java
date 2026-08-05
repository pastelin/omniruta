package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.request;

import org.springframework.web.multipart.MultipartFile;

public record HEADFileMessageRequest(MultipartFile file, Long jobId, String fileName) { }
