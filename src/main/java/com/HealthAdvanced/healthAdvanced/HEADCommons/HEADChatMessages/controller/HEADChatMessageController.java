package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.controller;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.entity.HEADWSRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.Service.HEADChatFileService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.Service.HEADChatService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.request.HEADFileMessageRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.response.wsResponse.HEADChatOpenConversationDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADModelBD.HEADFileAsset;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.Models.HEADFirebaseUpload.HEADUploadClientRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/v1")
public class HEADChatMessageController {

    private final HEADChatFileService headChatFileService;
    private final HEADChatService chatService;


    @PostMapping(value = "/upload/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadClient(@ModelAttribute HEADFileMessageRequest headFileMessageRequest) throws Exception {
        return ResponseEntity.ok(HEADApiResponse.ok(headChatFileService.uploadChatFile(headFileMessageRequest)));
    }

    @GetMapping("/open")
    public ResponseEntity<HEADApiResponse<HEADChatOpenConversationDto>> openJobChat(
            @RequestParam("jobId") Long jobId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size
    ) {
        return ResponseEntity.ok(HEADApiResponse.ok(chatService.openJobChat(jobId, page, size)));
    }
}
