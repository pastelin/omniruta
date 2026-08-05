package com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEHOUtilsFile;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADOccupationProfile;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADOccupationPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Enums.HEADStepCode;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Enums.HEADSubStepCode;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.services.iservices.HEADStepCurrentPersonalInterface;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADDocumentsRepository.HEADFileAssetRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADModelBD.HEADFileAsset;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.Models.HEHOMaps.HEADDocumentsMaps;
import com.HealthAdvanced.healthAdvanced.ModelsBD.DocumentsRepository.HEADDocumentCatalogueRepository;
import com.HealthAdvanced.healthAdvanced.ModelsBD.DocumentsRepository.HEADDocumentsOccupationsRepository;
import com.HealthAdvanced.healthAdvanced.ModelsBD.DocumentsRepository.HEADDocumentsRepository;
import com.HealthAdvanced.healthAdvanced.ModelsBD.PersonalUsers.HEADDocumentCatalogue;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Enums.HEADDocumentStatus;
import com.HealthAdvanced.healthAdvanced.ModelsBD.PersonalUsers.HEADDocumentOccupations;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class HEHOFileUploadUtil {

    @Autowired private HEADPersonalUserRepository personaUserRepository;
    @Autowired private HEADJwtGenerator headJwtGenerator;
    @Autowired private HEADDocumentCatalogueRepository headDocumentCatalogueRepository;
    @Autowired private HEADDocumentsOccupationsRepository headDocumentsOccupationsRepository;
    @Autowired private HEADDocumentsRepository headDocumentsRepository;
    @Autowired private HEADDocumentsMaps headDocumentsMaps;
    @Autowired private HEADOccupationPersonalUserRepository headOccupationPersonalUserRepository;
    @Autowired private HEADStepCurrentPersonalInterface headStepCurrentPersonalInterface;
    @Autowired private HEADFileAssetRepository headFileAssetRepository;

    // === NUEVO: registerDocs con upsert y soporte multiperfil ===
    @Transactional
    public List<HEADDocumentCatalogue> registerDocs(
            Integer idDocumentCatalogue,
            String extension,
            HEADPersonalUser user,
            @Nullable Long occProfileId,
            @Nullable HEADFileAsset fileAsset
    ) {
        var cat = headDocumentCatalogueRepository.findById(idDocumentCatalogue)
                .orElseThrow(() -> new HEADBadRequestException(
                        "Documento catálogo no válido: " + idDocumentCatalogue
                ));

        boolean single = (cat.getIdDocumentsRepeat() == null) || (cat.getIdDocumentsRepeat() <= 1);

        var existing = headDocumentsRepository.findActiveBySlot(
                user.getIdUser(),
                cat.getIdDocument(),
                occProfileId
        ).stream().findFirst().orElse(null);

        if (existing != null) {

            // REINTENTO DE DOCUMENTO RECHAZADO -> REEMPLAZA EL MISMO REGISTRO
            if (existing.getStatus() == HEADDocumentStatus.REJECTED) {
                HEADFileAsset oldAsset = existing.getFileAsset();

                existing.setNombreArchivo(cat.getNameDocument());
                existing.setExtension(extension);

                if (occProfileId != null) {
                    var occ = new HEADOccupationProfile();
                    occ.setIdOccupationProfile(occProfileId);
                    existing.setOccupationProfile(occ);
                } else {
                    existing.setOccupationProfile(null);
                }

                if (fileAsset != null) {
                    existing.setFileAsset(fileAsset);
                    existing.setMimeType(fileAsset.getMimeType());
                    existing.setSizeBytes(fileAsset.getSizeBytes());
                    existing.setStorageKey(fileAsset.getStorageKey());
                    existing.setUrl(fileAsset.getUrl());
                }

                existing.setStatus(HEADDocumentStatus.PENDING);
                existing.setReviewNotes(null);
                existing.setReviewedAt(null);
                existing.setReviewedByAdminId(null);
                existing.setUploadedAt(java.time.LocalDateTime.now());
                existing.setActive(true);

                headDocumentsRepository.save(existing);

                // Desactivar asset viejo solo si cambió
                if (oldAsset != null
                        && fileAsset != null
                        && !Objects.equals(oldAsset.getId(), fileAsset.getId())) {
                    headFileAssetRepository.delete(oldAsset);
                }

                return isCompletedDocuments(user, occProfileId);
            }

            if (existing.getStatus() == HEADDocumentStatus.PENDING && single) {
                throw new HEADBadRequestException("Ya existe un documento pendiente para revisión");
            }

            if (existing.getStatus() == HEADDocumentStatus.APPROVED && single) {
                throw new HEADBadRequestException("Ya existe un documento aprobado para este requisito");
            }
        }

        // INSERT NUEVO
        if (single) {
            headDocumentsRepository.deactivatePreviousForSameSlot(
                    user.getIdUser(),
                    cat.getIdDocument(),
                    occProfileId
            );
        }

        var doc = headDocumentsMaps.headDocumentsMap(cat, extension, user);

        if (occProfileId != null && doc.getOccupationProfile() == null) {
            var occ = new HEADOccupationProfile();
            occ.setIdOccupationProfile(occProfileId);
            doc.setOccupationProfile(occ);
        }

        if (fileAsset != null) {
            doc.setFileAsset(fileAsset);
            doc.setMimeType(fileAsset.getMimeType());
            doc.setSizeBytes(fileAsset.getSizeBytes());
            doc.setStorageKey(fileAsset.getStorageKey());
            doc.setUrl(fileAsset.getUrl());
        }

        doc.setStatus(HEADDocumentStatus.PENDING);
        doc.setActive(true);
        doc.setUploadedAt(java.time.LocalDateTime.now());

        headDocumentsRepository.save(doc);

        return isCompletedDocuments(user, occProfileId);
    }

    // === Versión original: manténla pero redirígela a la nueva si quieres ===
    public List<HEADDocumentCatalogue> registerDocs(Integer idDocumentCatalogue, String extension, HEADPersonalUser user) {
        return registerDocs(idDocumentCatalogue, extension, user, null, null);
    }

    // === Recalcula requeridos con flag "isSaveDocument" = APROBADO ===
    @Transactional
    public List<HEADDocumentCatalogue> isCompletedDocuments(HEADPersonalUser user, @Nullable Long occProfileId) {
        var required = getDocumentsRequired(user, occProfileId);

        var approved = headDocumentsRepository.findActiveByUserAndOccProfile(user.getIdUser(), occProfileId)
                .stream()
                .filter(d -> d.getStatus() == HEADDocumentStatus.APPROVED)
                .map(d -> d.getIdDocument().getIdDocument())
                .collect(Collectors.toSet());

        return required.stream()
                .peek(c -> c.setIsSaveDocument(approved.contains(c.getIdDocument())))
                .toList();
    }

    // === API sin parámetros (con token) → delega a la versión con usuario ===
    public List<HEADDocumentCatalogue> isCompletedDocuments() {
        String uid = headJwtGenerator.getUserNamePersonalUser();
        var user = personaUserRepository.findByUidUser(uid).orElse(null);
        if (user == null) return List.of();
        // Aquí puedes decidir si pasas occProfileId actual o null (global)
        return isCompletedDocuments(user, null);
    }

    // === Corrige: toma requeridos por uno o varios perfiles del usuario ===
    @Transactional
    private List<HEADDocumentCatalogue> getDocumentsRequired(HEADPersonalUser user, @Nullable Long occProfileId) {
        var links = headOccupationPersonalUserRepository.findByIdPersonalUser(user).orElseGet(ArrayList::new);
        var occIds = links.stream()
                .map(l -> l.getIdOccupationProfile().getIdOccupationProfile())
                .filter(id -> occProfileId == null || id.equals(occProfileId))
                .distinct()
                .toList();

        if (occIds.isEmpty()) return List.of();

        return headDocumentsOccupationsRepository.findRequiredCatalogsByOccProfiles(occIds)
                .stream()
                .distinct()
                .sorted(Comparator.comparing(HEADDocumentCatalogue::getIdDocument))
                .toList();
    }

    // === (Opcional) Missing explícitos (si quieres una lista) ===
    @Transactional
    public List<HEADDocumentCatalogue> documentsMissing(HEADPersonalUser user, @Nullable Long occProfileId) {
        var required = getDocumentsRequired(user, occProfileId);
        var approved = headDocumentsRepository.findActiveByUserAndOccProfile(user.getIdUser(), occProfileId)
                .stream()
                .filter(d -> d.getStatus() == HEADDocumentStatus.APPROVED)
                .map(d -> d.getIdDocument().getIdDocument())
                .collect(Collectors.toSet());

        var missing = required.stream()
                .filter(c -> !approved.contains(c.getIdDocument()))
                .toList();

        if (missing.isEmpty()) {
            headStepCurrentPersonalInterface.staffCompleteSub(
                    user.getIdUser(), HEADStepCode.REGISTER.name(), HEADSubStepCode.DOCUMENTATION.name());
        }
        return missing;
    }

    // === Se mantiene tu método canGoOnline (está bien) ===
    public boolean canGoOnline(Long userId, Long occProfileId) {
        var required = new HashSet<>(headDocumentsOccupationsRepository.findRequiredCatalogsByOccProfilesIds(occProfileId)).stream().filter(HEADDocumentOccupations::getIsRequired).map(doc -> doc.getHeadDocumentCatalogue().getIdDocument()).collect(Collectors.toSet());
        var approved = headDocumentsRepository.findApprovedDocIdsByUserAndOccProfile(userId, occProfileId);
        required.removeAll(approved);
        return required.isEmpty();
    }

    public Set<Integer> getRequiredDocIdsByOccProfile(Long occProfileId) {
        return headDocumentsOccupationsRepository.findRequiredDocIdsByOccProfile(occProfileId);
    }
}

