package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.api;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client.entity.HEADWsEnvelope;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.service.HEADStaffRatingService;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.api.HEADRatingWsEvents.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class HEADListenerSocketServerStaffRating {

    private final HEADStaffRatingService ratingService;

    private static String uuidOf(SocketIOClient c) { return c.get("userUuid"); }

    /** Staff pide su rating summary (MVP) */
    public void onGetMyRatingSummary(SocketIOClient staffClient, AckRequest ack) {
        Optional.ofNullable(uuidOf(staffClient)).ifPresentOrElse(staffUuid -> {
            try {
                var dto = ratingService.getSummaryForStaff(staffUuid);

                if (ack.isAckRequested()) {
                    ack.sendAckData(HEADWsEnvelope.ok(STAFF_RATING_OK, dto));
                }
                log.info("[STAFF_RATING_GET] staffUuid={} avg={} total={} bayes={}",
                        staffUuid, dto.avgRating(), dto.totalReviews(), dto.bayesianScore());
            } catch (Exception e) {
                log.error("[STAFF_RATING_GET] staffUuid={} err={}", staffUuid, e.getMessage(), e);
                if (ack.isAckRequested()) ack.sendAckData(HEADWsEnvelope.fail("STAFF_RATING_FAIL"));
            }
        }, staffClient::disconnect);
    }
}
