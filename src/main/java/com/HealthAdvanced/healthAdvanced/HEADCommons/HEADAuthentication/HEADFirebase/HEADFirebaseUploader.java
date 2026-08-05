package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADFirebase;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBusinessException;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class HEADFirebaseUploader {

    @Value("${firebase.bucket:}")      // permite vacío
    private String bucketProp;

    @Value("${firebase.credentials:}") // permite vacío
    private String credentialsProp;

    private final ResourceLoader loader;

    public HEADFirebaseUploader(ResourceLoader loader) {
        this.loader = loader;
    }

    @PostConstruct
    public void initFirebase() {
        try {
            // 1) Resolver bucket y credenciales: prop -> env var
            String bucket = firstNonBlank(bucketProp, System.getenv("FIREBASE_BUCKET"));
            String credPath = firstNonBlank(credentialsProp, System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));

            if (!StringUtils.hasText(bucket)) {
                throw new HEADBadRequestException("firebase.bucket no configurado (propiedad 'firebase.bucket' o env 'FIREBASE_BUCKET').");
            }
            if (!StringUtils.hasText(credPath)) {
                throw new HEADBadRequestException("Ruta de credenciales vacía (propiedad 'firebase.credentials' o env 'GOOGLE_APPLICATION_CREDENTIALS').");
            }

            // 2) Abrir soportando file:/, classpath:/ o ruta absoluta
            try (InputStream in = open(credPath)) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(in))
                        .setStorageBucket(bucket)
                        .build();

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                }

                // 3) Sanity check: fuerza acceso al bucket
                String name = StorageClient.getInstance().bucket().getName();
                System.out.println("✅ Firebase inicializado. Bucket: " + name + " | Credenciales: " + redacted(credPath));
            }

        } catch (Exception e) {
            // imprime causa real
            e.printStackTrace();
            throw new HEADBusinessException("Firebase init failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private InputStream open(String path) throws IOException {
        String toResolve = path.startsWith("classpath:") || path.startsWith("file:") ? path : "file:" + path;
        Resource res = loader.getResource(toResolve);
        if (!res.exists()) {
            throw new HEADBadRequestException("No existe: " + toResolve);
        }
        return res.getInputStream();
    }

    private static String firstNonBlank(String a, String b) {
        return StringUtils.hasText(a) ? a : (StringUtils.hasText(b) ? b : null);
    }

    private static String redacted(String p) {
        if (p == null) return "(null)";
        // oculta directorios, muestra solo nombre del archivo/prefijo
        int slash = Math.max(p.lastIndexOf('/'), p.lastIndexOf('\\'));
        return (slash >= 0 ? p.substring(slash + 1) : p);
    }
}
