package com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEHOUtilsFile;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class HEADSignedUrlService {
    public String signedUrl(String storageKey, Duration ttl) {
        var bucket = StorageClient.getInstance().bucket();
        Storage storage = bucket.getStorage();

        URL url = storage.signUrl(
                BlobInfo.newBuilder(bucket.getName(), storageKey).build(),
                ttl.toMinutes(), TimeUnit.MINUTES,
                Storage.SignUrlOption.withV4Signature()
        );
        return url.toString();
    }
}