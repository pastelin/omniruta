package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.api;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client.entity.HEADWsEnvelope;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.entities.request.HEADSubmitReviewRequest;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.service.HEADStaffRatingService;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import static com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.api.HEADRatingWsEvents.*;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HEADListenerSocketServerClientRating {

    private final HEADStaffRatingService ratingService;

    private static String uuidOf(SocketIOClient c) { return c.get("userUuid"); }

    public void onSubmitReview(SocketIOClient client, HEADSubmitReviewRequest req, AckRequest ack) {
        Optional.ofNullable(uuidOf(client)).ifPresentOrElse(uuid -> {
            try {
                var summary = ratingService.submitReview(uuid, req);
                if (ack.isAckRequested()) ack.sendAckData(HEADWsEnvelope.ok(REVIEW_OK, summary));
            } catch (Exception e) {
                log.error("[REVIEW_SUBMIT] clientUuid={} jobId={} err={}", uuid, req.jobId(), e.getMessage(), e);
                if (ack.isAckRequested()) ack.sendAckData(HEADWsEnvelope.fail("REVIEW_FAIL"));
            }
        }, client::disconnect);
    }
}

