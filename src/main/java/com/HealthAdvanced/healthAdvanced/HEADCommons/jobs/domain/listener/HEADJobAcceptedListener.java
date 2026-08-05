package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.listener;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.commons.interfaces.HEADWsEmitter;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.HEADStaffStateStore;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.entityDto.HEADStaffStateDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADGeocoding.entity.Dto.HEADRouteDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADGeocoding.service.HEADRoutingService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.domain.enums.HEADCallContextType;
import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.socket.HEADCallSignalingHandler;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.Dtos.HEADJobAcceptedAfterCommitEvent;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.Dtos.HEADJobAcceptedEvent;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.Dtos.HEADJobAcceptedVideo;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.Dtos.HEADJobStateChangedDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.service.HEADJobStateChangedEvent;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADServiceMode;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.dto.HEADNotificationCommand;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Enums.HEADNotificationType;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.interfaces.HEADNotificationSender;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADJobRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADDocumentsRepository.HEADFileAssetRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADCategory;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADOwnerType;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADModelBD.HEADFileAsset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.Map;

import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.HEADWsEvents.*;
import static com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState.ACCEPTED;
import static com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState.OFFERED;
import static com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.nameEvents.HEADNotificationTemplates.JOB_EN_ROUTE_CLIENT;

@Slf4j
@Component
@RequiredArgsConstructor
public class HEADJobAcceptedListener {

    private final HEADJobRepository jobRepo;
    private final HEADStaffStateStore staffState;
    private final HEADRoutingService routing;
    private final HEADWsEmitter emitter;
    private final ApplicationEventPublisher appEvents;
    private final HEADCallSignalingHandler callHandler;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onAccepted(HEADJobAcceptedEvent e) {
        log.info("[ACCEPT] publish AFTER_COMMIT jobId={} staff={}", e.jobId(), e.staffUuid());
        var job = jobRepo.findById(e.jobId())
                .orElseThrow(() -> new HEADBadRequestException("Job not found " + e.jobId()));

        var staffUuid = e.staffUuid();
        var st = staffState.get(staffUuid);

        if (st == null || st.lat() == null || st.lng() == null) {
            log.warn("[ROUTE] staff state missing for uuid={}", staffUuid);
            return;
        }
        if (job.getClientLat() == null || job.getClientLng() == null) {
            log.warn("[ROUTE] client location missing jobId={}", job.getId());
            return;
        }

        HEADRouteDto route = null;
        try {
            route = routing.routeStaffToClient(
                    st.lat(), st.lng(),
                    job.getClientLat(), job.getClientLng()
            );
        } catch (Exception ex) {
            log.error("[ROUTE] failed after accept jobId={} staffUuid={} err={}",
                    job.getId(), staffUuid, ex.toString(), ex);
        }

        var clientUuid = job.getClient().getUuIdUser();
        if (job.getServiceMode() == HEADServiceMode.VIDEO) {

            job.setState(HEADJobState.READY);
            if (route != null) {
                job.setStartAddress(route.startAddress());
                job.setEndAddress(route.endAddress());
            }
            jobRepo.save(job);

            emitter.toUser(staffUuid, VIDEO_TO_CLIENT,
                    HEADJobAcceptedVideo.acceptedVideo(job.getId(), clientUuid));

            if (clientUuid != null && !clientUuid.isBlank()) {
                emitter.emitToClient(clientUuid, VIDEO_TO_CLIENT,
                        HEADJobAcceptedVideo.acceptedVideo(job.getId(), staffUuid));
            }

            publishChange(job, ACCEPTED, Instant.now(), staffUuid);
            log.info("[ACCEPT] publish AFTER_COMMIT VIDEO jobId={} staff={}", e.jobId(), e.staffUuid());
            callHandler.startForJob(
                    job.getId(),
                    clientUuid,
                    staffUuid,
                    HEADCallContextType.JOB,
                    job.getRequest().getPkg().getId()
            );
        }
        else {

            try {
                if (route != null) {
                    job.setDistanceMeters(route.distanceMeters());
                    job.setDurationSeconds(route.durationSeconds());
                    job.setStartAddress(route.startAddress());
                    job.setEndAddress(route.endAddress());
                    job.setRouteNorthLat(route.northLat());
                    job.setRouteEastLng(route.eastLng());
                    job.setRouteSouthLat(route.southLat());
                    job.setRouteWestLng(route.westLng());
                }

                job.setState(HEADJobState.EN_ROUTE);

                jobRepo.save(job);

                emitter.toUser(staffUuid, ROUTE_TO_CLIENT, route);

                if (clientUuid != null && !clientUuid.isBlank()) {
                    emitter.emitToClient(clientUuid, ROUTE_TO_CLIENT, route);
                }

                publishChange(job, ACCEPTED, Instant.now(), staffUuid);
                log.info("[ACCEPT] publish AFTER_COMMIT ROUTE jobId={} staff={}", e.jobId(), e.staffUuid());
            } catch (Exception ex) {
                log.error("[ROUTE] failed after accept jobId={} staffUuid={} err={}",
                        job.getId(), staffUuid, ex.toString(), ex);
            }
        }
    }


    private void publishChange(HEADJob saved, HEADJobState prev, Instant at, String actorUuid) {
        appEvents.publishEvent(
                new HEADJobStateChangedEvent(
                        saved.getId(),
                        prev,
                        at,
                        actorUuid != null ? actorUuid : "SYSTEM"
                )
        );
    }

}

