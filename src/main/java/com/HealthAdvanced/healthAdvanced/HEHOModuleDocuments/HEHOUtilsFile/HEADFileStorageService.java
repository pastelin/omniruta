package com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEHOUtilsFile;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADDashboard.entity.HEADChangeStatusDocument;
import com.HealthAdvanced.healthAdvanced.HEADClient.geolocalizationClient.RepositoryClient.HEADClientWebSocketRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBusinessException;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADUpdateAvatarResponse;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.titleNameStaff.HEADNameFormatters;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.service.HEADPromotionsService;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.repository.HEADClientsRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.HEADWsEvents;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.commons.interfaces.HEADWsEmitter;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.DocumentsPersonal.service.HEADStaffRequirementsService;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADOccupationPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.services.iservices.HEADStepCurrentPersonalInterface;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADNextDTO;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Enums.HEADStepCode;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Enums.HEADSubStepCode;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADDocumentsRepository.HEADFileAssetRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.*;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADModelBD.HEADFileAsset;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.Models.HEADFirebaseUpload.*;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.Models.HEHODocResponse.HEADFileUploadErrorResponse;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.Models.HEHODocResponse.HEADUploadResponse;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.Models.HEHODocResponse.HEADUserInfoPersonal;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.Models.HEHOMaps.HEADDocumentsMaps;
import com.HealthAdvanced.healthAdvanced.ModelsBD.DocumentsRepository.HEADDocumentCatalogueRepository;
import com.HealthAdvanced.healthAdvanced.ModelsBD.DocumentsRepository.HEADDocumentsOccupationsRepository;
import com.HealthAdvanced.healthAdvanced.ModelsBD.DocumentsRepository.HEADDocumentsRepository;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Enums.HEADDocumentStatus;
import com.HealthAdvanced.healthAdvanced.ModelsBD.PersonalUsers.HEADDocumentCatalogue;
import com.HealthAdvanced.healthAdvanced.ModelsBD.PersonalUsers.HEADDocumentOccupations;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class HEADFileStorageService {
    private static final Logger log = LoggerFactory.getLogger(HEADFileStorageService.class);
    @Autowired
    private HEADFirebaseStorageService firebase;
    @Autowired
    private HEADFileAssetRepository repo;
    @Autowired
    private HEADJwtGenerator headJwtGenerator;
    @Autowired
    private HEADPersonalUserRepository headPersonalUserRepository;
    @Autowired
    private HEADClientsRepository headClientsRepository;
    @Autowired
    private HEHOFileUploadUtil hehoFileUploadUtil;
    @Autowired
    private HEADDocumentsMaps headDocumentsMaps;
    @Autowired
    private HEADPromotionsService headPromotionsService;
    @Autowired
    private HEADDocumentCatalogueRepository headDocumentCatalogueRepository;
    @Autowired
    private HEADDocumentsRepository headDocumentsRepository;
    @Autowired
    private HEADWsEmitter emitter;
    @Autowired
    private HEADOccupationPersonalUserRepository headOccupationPersonalUserRepository;
    @Autowired
    private HEADDocumentsOccupationsRepository headDocumentsOccupationsRepository;
    @Autowired
    private HEADStepCurrentPersonalInterface headStepCurrentPersonalInterface;
    @Autowired
    private HEADStaffRequirementsService headStaffRequirementsService;
    @Autowired
    private HEADStaffCredentialService staffCredentialService;
    @Autowired
    private HEADClientWebSocketRepository clientWebSocketRepository;

    public ResponseEntity<?> uploadSystemAsset(HEADUploadSystemRequest headUploadSystemRequest) throws Exception {
        var folder = headDocumentsMaps.folderFile(headUploadSystemRequest.headCategory());
        return new ResponseEntity<>(save(headUploadSystemRequest.file(), folder, HEADOwnerType.SYSTEM, headUploadSystemRequest.ownerId(), headUploadSystemRequest.headCategory(),headUploadSystemRequest.headScreenType() ,HEADVisibility.PUBLIC, -1,headUploadSystemRequest.subtitle(), headUploadSystemRequest.title(), headUploadSystemRequest.tags()), HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> uploadStaffDoc(HEADUploadStaffRequest req) throws Exception {
        String uid = headJwtGenerator.getUserNamePersonalUser();
        var error = new HEADFileUploadErrorResponse();

        if (uid == null) {
            error.setMessageError("Token Invalido");
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        var staff = headPersonalUserRepository.findByUidUser(uid).orElse(null);
        if (staff == null) {
            error.setMessageError("Usuario Invalido");
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        if (req.occupationProfileId() == null) {
            throw new HEADBadRequestException("occupationProfileId es requerido");
        }

        if (req.idDocumentCatalogue() == null) {
            throw new HEADBadRequestException("idDocumentCatalogue es requerido");
        }

        boolean staffHasProfile =
                headOccupationPersonalUserRepository
                        .existsStaffProfile(
                                staff.getIdUser(),
                                req.occupationProfileId()
                        );

        if (!staffHasProfile) {
            throw new HEADBadRequestException("La profesión no pertenece al staff");
        }

        Integer requestedDocId = req.idDocumentCatalogue();

        HEADDocumentCatalogue catalogue = headDocumentCatalogueRepository.findById(requestedDocId)
                .orElseThrow(() -> new HEADBadRequestException("Documento catálogo no válido: " + requestedDocId));

        HEADDocumentOccupations docOccupation =
                headDocumentsOccupationsRepository
                        .findDocOccupation(
                                req.occupationProfileId(),
                                requestedDocId
                        )
                        .orElseThrow(() -> new HEADBadRequestException(
                                "El documento no aplica para la profesión seleccionada"
                        ));

        String licenseNo = normalize(req.licenseNo());
        boolean requiresLicenseNo = Boolean.TRUE.equals(catalogue.getRequiresLicenseNo());

        if (requiresLicenseNo && licenseNo == null) {
            throw new HEADBadRequestException("El número de licencia/cédula es requerido para este documento");
        }

        String originalName = req.file().getOriginalFilename();
        String extension = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase()
                : "";

        var rawExt = catalogue.getExtension();
        var allowedExt = Arrays.stream(rawExt.split("/"))
                .map(String::trim)
                .map(s -> s.startsWith(".") ? s.substring(1) : s)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .toList();

        if (extension.isEmpty()) {
            String mimeType = req.file().getContentType();
            mimeType = mimeType != null ? mimeType : extension;
            extension = Arrays.stream(mimeType.split("/"))
                    .reduce((first, second) -> second)
                    .orElse("");
        }

        if (!allowedExt.contains(extension)) {
            throw new HEADBadRequestException(
                    "Extensión inválida. Se permiten: " + allowedExt.stream()
                            .map(e -> "." + e)
                            .collect(Collectors.joining(", "))
            );
        }

        Integer getIdDocument = req.headCategory() == HEADCategory.AVATAR ? -2 : req.idDocumentCatalogue();
        String folder = switch (req.headCategory()) {
            case AVATAR   -> "staffs/" + staff.getUidUser() + "/avatar";
            case DOC_STAFF-> "staffs/" + staff.getUidUser() + "/docs";
            default       -> "staffs/files";
        };

        var saved = save(
                req.file(),
                folder,
                HEADOwnerType.STAFF,
                staff.getIdUser(),
                req.headCategory(),
                null,
                HEADVisibility.PRIVATE,
                getIdDocument,
                null,
                null,
                null
        );

        hehoFileUploadUtil.registerDocs(
                req.idDocumentCatalogue(),
                saved.getMimeType(),
                staff,
                req.occupationProfileId(),
                saved
        );

        if (requiresLicenseNo) {
            staffCredentialService.upsertLicenseNo(
                    staff.getIdUser(),
                    req.occupationProfileId(),
                    licenseNo
            );
        }

        var resp = new HEADUploadResponse(
                true,
                "Documento cargado correctamente",
                saved.getUrl(),
                HEADDocumentStatus.PENDING.name(),
                requestedDocId
        );

        return ResponseEntity.ok(resp);
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Transactional
    public boolean deleteFile(String storagePath) {
        try {
            String uid = headJwtGenerator.getUserNamePersonalUser();
            HEADPersonalUser user = headPersonalUserRepository.findByUidUser(uid).orElse(null);
            if (user == null) { throw new HEADBadRequestException("Usuario Invalido"); }
            Bucket bucket = StorageClient.getInstance().bucket();
            Blob blob = bucket.get(storagePath);

            if (blob == null) {
                System.out.println("⚠️ El archivo no existe: " + storagePath);
                return false;
            }

            var getDocuments = headDocumentsRepository.findIdDocumentByStorageKeyAndIdUser(storagePath, user.getIdUser());
            if (!getDocuments.isEmpty()) {
                headDocumentsRepository.deleteAllById(getDocuments);
            }

            var isDeleteFile = blob.delete();
            var getFiles = repo.findIdByStorageKey(storagePath, user.getIdUser());
            if(!getFiles.isEmpty()) {
                repo.deleteAllById(getFiles);
            }
            return isDeleteFile;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    @Transactional
    public ResponseEntity<?> uploadClientDoc(HEADUploadClientRequest headUploadClientRequest) throws Exception {
        String getUUID = headJwtGenerator.getUserNamePersonalUser();
        HEADFileUploadErrorResponse error = new HEADFileUploadErrorResponse();
        if (getUUID == null) {
            error.setMessageError("Token Invalido");
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
        HEADClients clientCurrent = headClientsRepository.findByUuIdUser(getUUID).orElse(null);
        if (clientCurrent == null) {
            error.setMessageError("Usuario Invalido");
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
        Integer getIdDocument = headUploadClientRequest.headCategory() == HEADCategory.AVATAR ? -2 : headUploadClientRequest.idDocumentCatalogue();
        String folder = switch (headUploadClientRequest.headCategory()) {
            case AVATAR -> "clients/" + clientCurrent.getUuIdUser() + "/avatar";
            case DOC_CLIENT -> "clients/" + clientCurrent.getUuIdUser() + "/docs";
            case PRESCRIPTION -> "clients/" + clientCurrent.getUuIdUser() + "/prescription";
            default -> "clients/files";
        };
        return new ResponseEntity<>(save(headUploadClientRequest.file(), folder, HEADOwnerType.CLIENT, clientCurrent.getIdUser(), headUploadClientRequest.headCategory(), null, HEADVisibility.PRIVATE, getIdDocument, null, null, null), HttpStatus.OK);
    }

    @Transactional
    public HEADUpdateAvatarResponse uploadStaffAvatar(HEADUploadClientRequest headUploadClientRequest) {

        String getUUID = headJwtGenerator.getUserNamePersonalUser();
        HEADFileUploadErrorResponse error = new HEADFileUploadErrorResponse();
        if (getUUID == null) {
           throw new HEADBadRequestException("Token invalido");
        }
        HEADPersonalUser personalUserCurrent = headPersonalUserRepository.findByUidUser(getUUID).orElse(null);
        if (personalUserCurrent == null) {
            throw new HEADBadRequestException("Usuario Invalido");
        }
        Integer getIdDocument = headUploadClientRequest.headCategory() == HEADCategory.AVATAR ? -2 : headUploadClientRequest.idDocumentCatalogue();
        String folder = switch (headUploadClientRequest.headCategory()) {
            case AVATAR -> "staffs/" + personalUserCurrent.getUidUser() + "/avatar";
            case DOC_CLIENT -> "staffs/" + personalUserCurrent.getIdUser() + "/docs";
            default -> "staffs/files";
        };
        try {

            var getUrl = save(headUploadClientRequest.file(), folder, HEADOwnerType.STAFF, personalUserCurrent.getIdUser(), headUploadClientRequest.headCategory(), null, HEADVisibility.PRIVATE, getIdDocument, null, null, null);
            return new HEADUpdateAvatarResponse(
                    getUrl.getUrl()
            );
        } catch (Exception ex) {
            throw new HEADBusinessException(ex.getMessage());
        }
    }

    @Transactional
    public HEADFileAsset save(MultipartFile file, String folder,
                               HEADOwnerType ownerType, Long ownerId,
                               HEADCategory category, HEADScreenType headScreenType,
                               HEADVisibility visibility, Integer idDocumentCatalogue,
                               String subtitle, String title, String tags) throws Exception {

        HEADUploadResult up = firebase.uploadImage(file, folder);

        HEADFileAsset saved = Optional.of(new HEADFileAsset())
                .map(a -> {
                    a.setOwnerType(ownerType);
                    a.setOwnerId(ownerId);
                    a.setCategory(category);
                    a.setVisibility(visibility);
                    a.setStorageKey(up.storageKey());
                    a.setUrl(up.url());
                    a.setMimeType(up.contentType());
                    a.setSizeBytes(up.sizeBytes());
                    a.setActive(true);
                    a.setSortOrder(0);
                    a.setSubtitle(subtitle);
                    a.setScreenType(headScreenType);
                    a.setDocumentCatalogue(idDocumentCatalogue);
                    a.setTags(tags);
                    a.setContentType(file.getContentType());
                    a.setTitle(title);
                    try {
                        a.setContentLength(file.getResource().contentLength());
                    } catch (IOException ex) {
                        a.setContentLength(0L);
                    }
                    return a;
                })
                .map(repo::save)
                .orElseThrow();
        // Solo aplica para categorías reemplazables
        Optional.ofNullable(category)
                .filter(c -> c == HEADCategory.AVATAR || c == HEADCategory.PRESCRIPTION)
                .ifPresent(c ->
                        Optional.ofNullable(
                                        repo.findByOwnerTypeAndOwnerIdAndCategoryAndActive(
                                                ownerType,
                                                ownerId,
                                                c,
                                                true
                                        )
                                )
                                .map(list -> list.stream()
                                        .filter(x -> !Objects.equals(x.getId(), saved.getId()))
                                        .toList()
                                )
                                .filter(list -> !list.isEmpty())
                                .ifPresent(oldAssets -> {
                                    oldAssets.stream()
                                            .map(HEADFileAsset::getStorageKey)
                                            .filter(Objects::nonNull)
                                            .map(k -> up.bucket().get(k))
                                            .filter(Objects::nonNull)
                                            .forEach(blob -> {
                                                try { blob.delete(); } catch (Exception ignored) {}
                                            });
                                    var idsOlds = oldAssets.stream().map(HEADFileAsset::getId).toList();

                                    if (c == HEADCategory.PRESCRIPTION) {
                                        idsOlds.forEach(id -> clientWebSocketRepository.clearPrescriptionAssetByAssetId(id));
                                    }
                                    repo.deleteAllById(
                                            idsOlds
                                    );
                                })
                );

        return saved;
    }


    public ResponseEntity<?> listStaff(HEADFileStaffRequest headFileStaffRequest) {
        String getUUID = headJwtGenerator.getUserNamePersonalUser();
        HEADFileUploadErrorResponse error = new HEADFileUploadErrorResponse();
        if (getUUID == null) {
            error.setMessageError("Token Invalido");
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
        HEADPersonalUser staffCurrent = headPersonalUserRepository.findByUidUser(getUUID).orElse(null);
        if (staffCurrent == null) {
            error.setMessageError("Usuario Invalido");
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(repo.findByOwnerTypeAndOwnerIdAndActive(HEADOwnerType.STAFF, staffCurrent.getIdUser(), headFileStaffRequest.active()), HttpStatus.OK);
    }

    public ResponseEntity<?> listClient(HEADFileClientRequest headFileClientRequest) {
        String getUUID = headJwtGenerator.getUserNamePersonalUser();
        HEADFileUploadErrorResponse error = new HEADFileUploadErrorResponse();
        if (getUUID == null) {
            error.setMessageError("Token Invalido");
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
        HEADClients clientCurrent = headClientsRepository.findByUuIdUser(getUUID).orElse(null);
        if (clientCurrent == null) {
            error.setMessageError("Usuario Invalido");
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(repo.findByOwnerTypeAndOwnerIdAndActive(HEADOwnerType.CLIENT, clientCurrent.getIdUser(), headFileClientRequest.active()), HttpStatus.OK);
    }

    @Transactional
    public void approveDocument(HEADChangeStatusDocument headChangeStatusDocument) {
        var userId = headChangeStatusDocument.getUserId();

        var staffUser = headPersonalUserRepository.findById(userId).orElse(null);
        if (staffUser == null) {
            throw new HEADBadRequestException("Usuario Invalido");
        }

        headDocumentsRepository.updateStatusForIdDocument(
                userId,
                headChangeStatusDocument.getDocumentId(),
                headChangeStatusDocument.getIdOccProfile(),
                headChangeStatusDocument.getStatus(),
                headChangeStatusDocument.getMotiveNote()
        );

        var profilesSelected = headOccupationPersonalUserRepository
                .findByIdPersonalUser(staffUser)
                .orElse(new ArrayList<>());

        var idProfiles = profilesSelected.stream()
                .map(p -> p.getIdOccupationProfile().getIdOccupationProfile())
                .toList();

        boolean canGoOnline = idProfiles.stream()
                .allMatch(idProfile -> hehoFileUploadUtil.canGoOnline(userId, idProfile));

        if (canGoOnline) {
            var occCode = headOccupationPersonalUserRepository.findPrimaryOccupationCodeOrNull(staffUser.getIdUser());
            var nameStaff = HEADNameFormatters.buildStaffNameLastName(staffUser, occCode);
            headStepCurrentPersonalInterface.staffCompleteSub(
                    userId,
                    HEADStepCode.REGISTER.name(),
                    HEADSubStepCode.DOCUMENTATION.name()
            );
            emitter.toUser(
                    staffUser.getUidUser(),
                    HEADWsEvents.STAFF_COMPLETED_SUCCESS,
                    new HEADUserInfoPersonal(nameStaff, staffUser.getEmail(), staffUser.getTelefono())
            );
        }

        HEADNextDTO next = headStepCurrentPersonalInterface.statusStaff(userId).next();

        var payLoad = headStaffRequirementsService.getRequirementsBulk(staffUser)
                .stream()
                .peek(updateRes -> updateRes.setNextStep(next))
                .toList();

        emitter.toUser(
                staffUser.getUidUser(),
                HEADWsEvents.STAFF_VERIFICATION_UPDATED,
                payLoad
        );
    }
}
