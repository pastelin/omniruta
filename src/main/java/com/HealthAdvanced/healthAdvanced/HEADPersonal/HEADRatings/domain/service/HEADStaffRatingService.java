package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.service;

import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.repository.HEADClientsRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.commons.interfaces.HEADWsEmitter;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADUtils.HEADPageMapper;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.service.HEADJobStateChangedEvent;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADJobRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.entities.projections.HEADRatingCountProjection;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.entities.projections.HEADRecentReviewProjection;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.entities.request.HEADCreateStaffReviewRequest;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.entities.request.HEADSubmitReviewRequest;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.entities.response.*;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.model.HEADStaffRatingSummary;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.model.HEADStaffReview;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.repository.HEADStaffRatingSummaryRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.repository.HEADStaffReviewRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.enums.HEADReviewState;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.api.HEADRatingWsEvents.*;
import static java.time.Instant.now;


@Slf4j
@Service
@RequiredArgsConstructor
public class HEADStaffRatingService {

    private final HEADStaffReviewRepository reviewRepo;
    private final HEADStaffRatingSummaryRepository summaryRepo;
    private final HEADJobRepository jobRepo;
    private final HEADWsEmitter emitter;
    private final HEADPersonalUserRepository staffRepo;
    private final ApplicationEventPublisher appEvents;
    private final HEADJwtGenerator jwt;

    private static final int M = 5;
    private static final ZoneId MX_ZONE = ZoneId.of("America/Mexico_City");
    private static final DateTimeFormatter REVIEW_DATE_FORMAT =
            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH).withZone(MX_ZONE);

    private static final int MIN_REVIEWS_FOR_RANKING = 10;
    private static final long MIN_PEERS_FOR_RANKING = 20L;

    @Transactional
    public HEADStaffRatingSummaryDto submitReview(String clientUuid, HEADSubmitReviewRequest req) {

        var job = jobRepo.findByIdForUpdate(req.jobId())
                .filter(j -> Optional.ofNullable(j.getClient())
                        .map(HEADClients::getUuIdUser)
                        .filter(clientUuid::equals)
                        .isPresent())
                .filter(j -> HEADJobState.COMPLETED.equals(j.getState()))
                .filter(j -> Objects.nonNull(j.getStaffUser()))
                .filter(j -> !reviewRepo.existsByJob_Id(j.getId()))
                .orElseThrow(() -> new HEADBadRequestException("Review cannot be created for this job"));

        var review = new HEADStaffReview();
        review.setJob(job);
        review.setIdPersonalUser(job.getStaffUser());
        review.setIdUserClient(job.getClient());
        review.setRating(req.rating());
        review.setComment(req.comment());
        reviewRepo.save(review);

        job.setStaffReview(review);
        job.setReviewState(HEADReviewState.REVIEWED);
        job.setReviewedAt(now());
        jobRepo.save(job);

        long staffId = job.getStaffUser().getIdUser();

        var summary = summaryRepo.findByIdForUpdate(staffId)
                .orElseGet(() -> newSummary(job.getStaffUser()));

        int total = Optional.ofNullable(summary.getTotalReviews()).orElse(0) + 1;
        int sum = Optional.ofNullable(summary.getSumRating()).orElse(0) + req.rating();

        BigDecimal avg = BigDecimal.valueOf(sum)
                .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP);

        BigDecimal globalAvg = BigDecimal.valueOf(reviewRepo.platformAvgRating());

        BigDecimal bayes = avg.multiply(BigDecimal.valueOf(total))
                .add(globalAvg.multiply(BigDecimal.valueOf(M)))
                .divide(BigDecimal.valueOf(total + M), 4, RoundingMode.HALF_UP);

        summary.setTotalReviews(total);
        summary.setSumRating(sum);
        summary.setAvgRating(avg.setScale(2, RoundingMode.HALF_UP));
        summary.setBayesianScore(bayes.setScale(2, RoundingMode.HALF_UP));

        summaryRepo.save(summary);

        var breakdown = normalizeStars(reviewRepo.countByRatingForStaff(staffId));

        var dto = new HEADStaffRatingSummaryDto(
                staffId,
                Optional.ofNullable(summary.getAvgRating()).map(BigDecimal::doubleValue).orElse(0.0),
                (long) total,
                Optional.ofNullable(summary.getBayesianScore()).map(BigDecimal::doubleValue).orElse(0.0),
                breakdown
        );

        var staffUuid = job.getStaffUser().getUidUser();

        emitter.toUser(
                staffUuid,
                STAFF_RATING_UPDATED,
                Map.of("type", "REFRESH_MY_RATING")
        );

        publishChange(job, job.getState(), now(), clientUuid);
        return dto;
    }

    @Transactional(readOnly = true)
    public HEADStaffRatingSummaryDto getSummaryForStaff(String staffUuid) {

        var staff = staffRepo.findByUidUser(staffUuid)
                .orElseThrow(() -> new HEADBadRequestException("Staff not found for uuid=" + staffUuid));

        long staffId = staff.getIdUser();

        return summaryRepo.findByIdPersonalUser_IdUser(staffId)
                .map(summary -> new HEADStaffRatingSummaryDto(
                        staffId,
                        Optional.ofNullable(summary.getAvgRating()).map(BigDecimal::doubleValue).orElse(0.0),
                        Optional.ofNullable(summary.getTotalReviews()).map(Integer::longValue).orElse(0L),
                        Optional.ofNullable(summary.getBayesianScore()).map(BigDecimal::doubleValue).orElse(0.0),
                        normalizeStars(reviewRepo.countByRatingForStaff(staffId))
                ))
                .orElseGet(() -> new HEADStaffRatingSummaryDto(
                        staffId,
                        0.0,
                        0L,
                        0.0,
                        normalizeStars(List.of())
                ));
    }

    @Transactional(readOnly = true)
    public HEADMyRatingResponse getMyRatingForStaff(Integer page, Integer size) {

        String staffUuid = jwt.getUserNamePersonalUser();
        var staff = staffRepo.findByUidUser(staffUuid)
                .orElseThrow(() -> new HEADBadRequestException("Staff not found for uuid=" + staffUuid));

        long staffId = staff.getIdUser();

        var summary = summaryRepo.findByIdPersonalUser_IdUser(staffId).orElse(null);

        int totalReviews = Optional.ofNullable(summary)
                .map(HEADStaffRatingSummary::getTotalReviews)
                .orElse(0);

        double overallRating = Optional.ofNullable(summary)
                .map(HEADStaffRatingSummary::getAvgRating)
                .map(BigDecimal::doubleValue)
                .orElse(0.0);

        var distribution = buildDistribution(
                normalizeStars(reviewRepo.countByRatingForStaff(staffId)),
                totalReviews
        );

        var stats = new HEADRatingStatsResponse(
                calculateWeeklyIncrease(staffId),
                calculateTopPercentage(staffId)
        );

        int safePage = Optional.ofNullable(page).filter(p -> p >= 0).orElse(0);
        int safeSize = HEADPageMapper.clampSize(size);

        Page<HEADRecentReviewProjection> reviewPage = reviewRepo.findRecentReviewsByStaffId(
                staffId,
                PageRequest.of(safePage, safeSize)
        );

        var reviews = HEADPageMapper.map(reviewPage, this::toReviewViewDto);

        return new HEADMyRatingResponse(
                overallRating,
                totalReviews,
                distribution,
                stats,
                reviews
        );
    }

    private HEADStaffRatingSummary newSummary(HEADPersonalUser staffUser) {
        var summary = new HEADStaffRatingSummary();
        summary.setIdPersonalUser(staffUser);
        summary.setTotalReviews(0);
        summary.setSumRating(0);
        summary.setAvgRating(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        summary.setBayesianScore(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        return summary;
    }

    private List<HEADRatingDistributionResponse> buildDistribution(Map<Integer, Long> starsMap, int totalReviews) {
        return IntStream.iterate(5, i -> i >= 1, i -> i - 1)
                .mapToObj(stars -> {
                    long count = starsMap.getOrDefault(stars, 0L);
                    int percentage = Optional.of(totalReviews)
                            .filter(total -> total > 0)
                            .map(total -> (int) Math.round((count * 100.0) / total))
                            .orElse(0);

                    return new HEADRatingDistributionResponse(
                            stars,
                            Math.toIntExact(count),
                            percentage
                    );
                })
                .toList();
    }

    private HEADReviewViewResponse toReviewViewDto(HEADRecentReviewProjection row) {
        return new HEADReviewViewResponse(
                Math.toIntExact(Optional.ofNullable(row.getId()).orElse(0L)),
                Stream.of(row.getNombre(), row.getPaterno())
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .collect(Collectors.joining(" ")),
                Optional.ofNullable(row.getRating()).orElse(0),
                Optional.ofNullable(row.getComment()).orElse(""),
                Optional.ofNullable(row.getCreatedAt()).map(REVIEW_DATE_FORMAT::format).orElse(""),
                Optional.ofNullable(row.getServiceName()).orElse("")
        );
    }

    private String calculateWeeklyIncrease(Long staffId) {
        Instant now = Instant.now();
        Instant currentFrom = now.minus(7, ChronoUnit.DAYS);
        Instant previousFrom = now.minus(14, ChronoUnit.DAYS);

        double current = Optional.ofNullable(reviewRepo.avgRatingForStaffBetween(staffId, currentFrom, now)).orElse(0.0);
        double previous = Optional.ofNullable(reviewRepo.avgRatingForStaffBetween(staffId, previousFrom, currentFrom)).orElse(0.0);

        return String.format(Locale.US, "%+.2f", current - previous);
    }

    private static Map<Integer, Long> normalizeStars(List<HEADRatingCountProjection> rows) {
        Map<Integer, Long> base = IntStream.rangeClosed(1, 5)
                .boxed()
                .collect(Collectors.toMap(
                        Function.identity(),
                        i -> 0L,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        return Stream.concat(
                        base.entrySet().stream(),
                        rows.stream()
                                .filter(Objects::nonNull)
                                .filter(r -> Objects.nonNull(r.getRating()))
                                .map(r -> Map.entry(
                                        r.getRating(),
                                        Optional.ofNullable(r.getCnt()).orElse(0L)
                                ))
                )
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> newValue,
                        LinkedHashMap::new
                ));
    }

    private void publishChange(HEADJob saved, HEADJobState prev, Instant at, String actorUuid) {
        appEvents.publishEvent(
                new HEADJobStateChangedEvent(
                        saved.getId(),
                        prev,
                        at,
                        Optional.ofNullable(actorUuid).orElse("SYSTEM")
                )
        );
    }

    private String calculateTopPercentage(Long staffId) {
        return summaryRepo.findByIdPersonalUser_IdUser(staffId)
                .filter(summary -> Optional.ofNullable(summary.getTotalReviews()).orElse(0) >= MIN_REVIEWS_FOR_RANKING)
                .flatMap(summary ->
                        staffRepo.findPrimaryOccupationProfileId(staffId)
                                .map(profileId -> Map.entry(profileId, summary))
                )
                .filter(entry ->
                        summaryRepo.countPeersByOccupationProfile(
                                entry.getKey(),
                                MIN_REVIEWS_FOR_RANKING
                        ) >= MIN_PEERS_FOR_RANKING
                )
                .map(entry -> {
                    Long occupationProfileId = entry.getKey();
                    HEADStaffRatingSummary summary = entry.getValue();

                    long betterPeers = summaryRepo.countBetterPeersByOccupationProfile(
                            occupationProfileId,
                            MIN_REVIEWS_FOR_RANKING,
                            Optional.ofNullable(summary.getBayesianScore()).orElse(BigDecimal.ZERO),
                            Optional.ofNullable(summary.getTotalReviews()).orElse(0),
                            staffId
                    );

                    long totalPeers = summaryRepo.countPeersByOccupationProfile(
                            occupationProfileId,
                            MIN_REVIEWS_FOR_RANKING
                    );

                    long rank = betterPeers + 1;
                    long topPercent = (long) Math.ceil((rank * 100.0) / totalPeers);

                    return formatTopPercentage(topPercent);
                })
                .orElse("");
    }

    private String formatTopPercentage(long topPercent) {
        return Stream.of(5L, 10L, 25L)
                .filter(limit -> topPercent <= limit)
                .findFirst()
                .map(limit -> "Top " + limit + "%")
                .orElse("Top " + topPercent + "%");
    }
}
