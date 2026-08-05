package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADSessions.service;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADConstants.HEADHeadersConstants;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADSessions.enums.HEADAuthLevel;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADSessions.enums.HEADHeadStep;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADSessions.enums.HEADRegStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class HEADSessionService {

    private final HttpServletRequest request;

    private static final String PREFIX = "authlvl:";
    private static final Duration TTL = Duration.ofHours(48);

    private final StringRedisTemplate redis;

    public void set(String uuidUser,HEADAuthLevel lvl) {
        String deviceId = request.getHeader(HEADHeadersConstants.DEVICE_ID);
        redis.opsForValue().set(key(uuidUser, deviceId), lvl.name(), TTL);
    }
    public HEADAuthLevel get(String uuidUser) {
        String deviceId = request.getHeader(HEADHeadersConstants.DEVICE_ID);
        Object v = redis.opsForValue().get(key(uuidUser, deviceId));
        return v == null ? HEADAuthLevel.NONE : HEADAuthLevel.valueOf(v.toString());
    }
    public void clear(String uuidUser, String deviceId) {
        redis.delete(key(uuidUser, deviceId));
    }
    private String key(String u, String d) { return PREFIX + u + ":" + d; }

    public HEADHeadStep decideStep(String uuidUser, HEADRegStatus reg) {
        var lvl = this.get(uuidUser);
        return switch (lvl) {
            case NONE -> HEADHeadStep.REGISTER;
            case OTP_VERIFIED -> (reg == HEADRegStatus.REGISTER ? HEADHeadStep.LOGIN : HEADHeadStep.REGISTER);
            case PASSWORD_VERIFIED -> (reg == HEADRegStatus.REGISTER ? HEADHeadStep.DASHBOARD : HEADHeadStep.REGISTER);
        };
    }
}
