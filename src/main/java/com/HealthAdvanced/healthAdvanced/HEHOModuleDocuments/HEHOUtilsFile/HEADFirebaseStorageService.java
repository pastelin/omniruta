package com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEHOUtilsFile;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.Models.HEADFirebaseUpload.HEADUploadResult;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Service
public class HEADFirebaseStorageService {

    public HEADUploadResult uploadImage(MultipartFile file, String prefix) throws Exception {
        if (file.isEmpty()) throw new HEADBadRequestException("Archivo vacío");
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new HEADBadRequestException("Selecciona archivos correspondientes (PDF 0 imagen)");
        }

        String ext = switch (contentType) {
            case MediaType.IMAGE_PNG_VALUE -> ".png";
            case MediaType.IMAGE_JPEG_VALUE -> ".jpg";
            case MediaType.APPLICATION_PDF_VALUE -> ".pdf";
            case "image/webp" -> ".webp";
            default -> "";
        };

        String folder = (prefix == null || prefix.isBlank()) ? "" :
                (prefix.endsWith("/") ? prefix : prefix + "/");

        String name = UUID.randomUUID().toString() + ext;
        String storageKey = folder + name; // <<--- lo guardamos en BD

        String downloadToken = UUID.randomUUID().toString();

        Bucket bucket = StorageClient.getInstance().bucket();
        BlobInfo blobInfo = BlobInfo.newBuilder(bucket.getName(), storageKey)
                .setContentType(contentType)
                .setMetadata(Map.of("firebaseStorageDownloadTokens", downloadToken))
                .setCacheControl("public, max-age=60")
                .build();
        bucket.getStorage().create(blobInfo, file.getBytes());

        String encoded = URLEncoder.encode(storageKey, StandardCharsets.UTF_8);
        String url = "https://firebasestorage.googleapis.com/v0/b/" +
                bucket.getName() + "/o/" + encoded + "?alt=media&token=" + downloadToken;
        return new HEADUploadResult(storageKey, url, contentType, file.getSize(), bucket);
    }


}
