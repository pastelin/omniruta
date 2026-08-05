package com.HealthAdvanced.healthAdvanced.HEADPersonal.DocumentsPersonal.service;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.DocumentsPersonal.entity.*;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.DocumentsPersonal.enums.HEADBlockingReasonCode;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.DocumentsPersonal.enums.HEADDocAction;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADOccupationPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Repository.HEADOccupationsProfilesRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADModelBD.HEADFileAsset;
import com.HealthAdvanced.healthAdvanced.ModelsBD.DocumentsRepository.HEADDocumentCatalogueRepository;
import com.HealthAdvanced.healthAdvanced.ModelsBD.DocumentsRepository.HEADDocumentsOccupationsRepository;
import com.HealthAdvanced.healthAdvanced.ModelsBD.DocumentsRepository.HEADDocumentsRepository;
import com.HealthAdvanced.healthAdvanced.ModelsBD.DocumentsRepository.HEADStaffCredentialRepository;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Enums.HEADCredentialType;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Enums.HEADDocumentStatus;
import com.HealthAdvanced.healthAdvanced.ModelsBD.PersonalUsers.HEADDocumentCatalogue;
import com.HealthAdvanced.healthAdvanced.ModelsBD.PersonalUsers.HEADDocuments;
import com.google.api.client.util.Value;
import com.google.cloud.ReadChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.channels.Channels;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;
import static com.HealthAdvanced.healthAdvanced.ModelsBD.Enums.HEADDocumentStatus.NOT_UPLOADED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class HEADStaffRequirementsService {
    private final HEADDocumentsOccupationsRepository docOccRepo;
    private final HEADDocumentCatalogueRepository catalogueRepo;
    private final HEADDocumentsRepository documentsRepo;
    private final HEADOccupationsProfilesRepository occProfileRepo;
    private final HEADOccupationPersonalUserRepository headOccupationPersonalUserRepository;
    private final HEADStaffCredentialRepository credentialRepo;



    // Config globales (puedes leerlas de properties si quieres)
    private static final int DEFAULT_MAX_SIZE_MB = 10;

    @Transactional(readOnly = true)
    public List<HEADRequirementsByProfileResponse> getRequirementsBulk(HEADPersonalUser userId) {
        List<Long> occProfileIds = headOccupationPersonalUserRepository.findByIdPersonalUser(userId).orElse(new ArrayList<>()).stream().map(occProfile -> occProfile.getIdOccupationProfile().getIdOccupationProfile()).toList();
        if (occProfileIds.isEmpty()) return List.of();
        return occProfileIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(id -> getRequirementsDetailed(userId.getIdUser(), id))
                .toList();
    }

    @Transactional(readOnly = true)
    public HEADRequirementsByProfileResponse getRequirementsDetailed(Long userId, Long occProfileId) {

        // 0) Nombre del perfil (para mostrar título en UI)
        var occ = occProfileRepo.findById(occProfileId).orElse(null);
        String occName = (occ != null) ? occ.getNameTypeProfile() : null;

        // 1) IDs requeridos por el perfil
        var requiredIds = new ArrayList<>(docOccRepo.findRequiredDocIdsByOccProfile(occProfileId));
        requiredIds.sort(Integer::compareTo);

        // early return si no hay requisitos (poco probable)
        if (requiredIds.isEmpty()) {
            var resp = new HEADRequirementsByProfileResponse();
            resp.setOccupationProfileId(occProfileId);
            resp.setOccupationProfileName(occName);
            resp.setCanGoOnline(true);
            resp.setBlocking(new HEADBlockingDTO(null, null, HEADBlockingReasonCode.NONE));
            resp.setDocuments(List.of());
            return resp;
        }

        var occProf = docOccRepo.findRequiredCatalogsByOccProfilesIds(occProfileId);

        var licenseNo = credentialRepo.findCredential(
                userId, occProfileId, HEADCredentialType.LICENSE_NO).orElse(null);
        // 2) Catálogos con detalles
        var catalogues = catalogueRepo.findAllById(requiredIds).stream()
                .collect(Collectors.toMap(HEADDocumentCatalogue::getIdDocument, c -> c));

        // 3) Documentos del usuario activos para esos IDs (en ese perfil)
        var userDocs = documentsRepo.findActiveByUserOccProfileAndDocIds(userId, occProfileId, requiredIds);
        var byDocId = userDocs.stream()
                .collect(Collectors.toMap(d -> d.getIdDocument().getIdDocument(), d -> d, (a, b) -> a));

        requiredIds = requiredIds.stream().filter(doc -> catalogues.get(doc).getIsVisibility()).collect(Collectors.toCollection(ArrayList::new));
        // 4) Construir items
        var items = new ArrayList<HEADDocRequirementItemDTO>();
        var approvedIds = new HashSet<Integer>(); // llenamos durante el loop

        requiredIds.forEach(docId -> {
            var cat = catalogues.get(docId);
            var d = byDocId.get(docId);

            var occPr = occProf.stream().filter(doc -> Objects.equals(doc.getHeadDocumentCatalogue().getIdDocument(), cat.getIdDocument())).findFirst().orElse(null);
            var item = new HEADDocRequirementItemDTO();
            item.setDocumentId(docId);
            item.setName(nullSafe(cat.getNameDocument()));
            item.setDescription(nullSafe(cat.getDescriptionDocument()));
            item.setTypeFile(nullSafe(cat.getTypeFile()));
            item.setAllowedExtensions(parseExts(cat.getExtension()));
            item.setRequired(occPr != null ? occPr.getIsRequired() : true);

            item.setRequiresLicenseNo(Boolean.TRUE.equals(cat.getRequiresLicenseNo()));
            item.setLicenseLabel(Boolean.TRUE.equals(cat.getRequiresLicenseNo())
                    ? licenseNo != null ? licenseNo.getValue() : "Número de cédula"
                    : null);

            boolean repeatAllowed = (cat.getIdDocumentsRepeat() != null && cat.getIdDocumentsRepeat() > 1);
            item.setRepeatAllowed(repeatAllowed);
            item.setMaxRepeats(cat.getIdDocumentsRepeat());

            if (d == null) {
                item.setStatus(NOT_UPLOADED.name());
                item.setUploaded(false);
                item.setFile(null);
                item.setUploadedAt(null);
                item.setReviewedAt(null);
                item.setReviewNotes(null);
                item.setUi(buildUiFlags(cat, null));
                item.setActions(resolveActions(cat, d));
            } else {
                item.setUploaded(true);
                item.setStatus(d.getStatus().name());
                item.setUploadedAt(d.getUploadedAt());
                item.setReviewedAt(d.getReviewedAt());
                item.setReviewNotes(d.getReviewNotes());
                item.setActions(resolveActions(cat, d));

                if (d.getStatus() == HEADDocumentStatus.APPROVED) {
                    approvedIds.add(docId);
                }

                HEADFileInfoDTO finfo = null;
                if (d.getFileAsset() != null) {
                    finfo = new HEADFileInfoDTO(
                            d.getFileAsset().getId(),
                            d.getUrl(),
                            d.getStorageKey(),
                            d.getMimeType(),
                            d.getSizeBytes()
                    );
                }
                item.setFile(finfo);
                item.setUi(buildUiFlags(cat, d));
            }

            items.add(item);
        });

        // 5) canGoOnline = todos aprobados
        boolean canGoOnline = requiredIds.stream().allMatch(approvedIds::contains);

        // 6) Blocking info
        HEADBlockingDTO blocking = canGoOnline
                ? new HEADBlockingDTO(null, null, HEADBlockingReasonCode.NONE)
                : new HEADBlockingDTO(
                "Completa tu verificación",
                "Sube y espera aprobación de los documentos faltantes para activar tu perfil.",
                HEADBlockingReasonCode.MISSING_REQUIRED_DOCS
        );

        // 7) Respuesta final
        var resp = new HEADRequirementsByProfileResponse();
        resp.setOccupationProfileId(occProfileId);
        resp.setOccupationProfileName(occName);
        resp.setCanGoOnline(canGoOnline);
        resp.setBlocking(blocking);
        resp.setDocuments(items);
        return resp;
    }

    // ===== Helpers =====

    private List<String> parseExts(String extsCsv) {
        if (extsCsv == null || extsCsv.isBlank()) return List.of();
        return Arrays.stream(extsCsv.split(","))
                .map(s -> s.trim().toLowerCase())
                .filter(s -> !s.isBlank())
                .toList();
    }

    private HEADUiFlagsDTO buildUiFlags(HEADDocumentCatalogue cat, HEADDocuments d) {
        boolean repeatAllowed = (cat.getIdDocumentsRepeat() != null && cat.getIdDocumentsRepeat() > 1);
        String status = (d == null) ? "NOT_UPLOADED" : d.getStatus().name();

        boolean b = repeatAllowed || "REJECTED".equals(status) || "NOT_UPLOADED".equals(status);

        String hint = hintFor(cat);

        return new HEADUiFlagsDTO(hint, DEFAULT_MAX_SIZE_MB, b, b);
    }

    private String hintFor(HEADDocumentCatalogue cat) {
        // Puedes sofisticarlo por tipo/extensión; dejo un básico
        String type = nullSafe(cat.getTypeFile()).toLowerCase();
        if (type.contains("pdf")) return "Sube un PDF legible";
        if (type.contains("image")) return "Sube una imagen clara";
        return "Sube un archivo válido";
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }

    private List<HEADDocAction> resolveActions(HEADDocumentCatalogue cat, HEADDocuments d) {
        var actions = new ArrayList<HEADDocAction>();
        boolean repeatAllowed = cat.getIdDocumentsRepeat() != null && cat.getIdDocumentsRepeat() > 1;
        var status = (d == null) ? NOT_UPLOADED : d.getStatus();

        switch (status) {
            case NOT_UPLOADED -> actions.add(HEADDocAction.UPLOAD);

            case PENDING -> {
                actions.add(HEADDocAction.VIEW);
                // no permitir replace/delete mientras está pendiente
            }

            case APPROVED -> {
                actions.add(HEADDocAction.VIEW);
                if (repeatAllowed) {
                    actions.add(HEADDocAction.REPLACE);
                    actions.add(HEADDocAction.DELETE);
                }
            }

            case REJECTED -> {
                actions.add(HEADDocAction.VIEW);
                actions.add(HEADDocAction.RETRY);
                actions.add(HEADDocAction.DELETE);
            }
        }
        return actions;
    }

}