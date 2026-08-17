package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADOtpService.services;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADOtpService.Dtos.*;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADOtpService.contracts.IHEADOtpService;
import com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.HEADCodeSecurityInterfaces.HEADCodeSecurityInputRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HEADOtpService implements IHEADOtpService {
    @Autowired
    private HEADCodeSecurityInputRepository codeSecurityRepo;

    private final StringRedisTemplate redis;
    private final SecureRandom rnd = new SecureRandom();

    private static final int TTL_SECONDS = 300;   // 5 min
    private static final int COOLDOWN_SECONDS = 30;
    private static final int MAX_ATTEMPTS = 5;

    private String genCode() { return String.format("%06d", rnd.nextInt(1_000_000)); }

    @Override
    public HEADOtpStarRes start(HEADOtpRequest headOtpRequest,String role) {
        // Rate limit por identificador (máx 5 por hora)
        String rateKey = "otp:rate:" + headOtpRequest.identifier();
        Long sent = redis.opsForValue().increment(rateKey);
        if (sent != null && sent == 1) redis.expire(rateKey, Duration.ofHours(1));
        if (sent != null && sent > 10) throw new HEADBadRequestException("Demasiados envíos");

        String code = genCode();
        String hash = org.mindrot.jbcrypt.BCrypt.hashpw(code, org.mindrot.jbcrypt.BCrypt.gensalt(10));

        String txId = java.util.UUID.randomUUID().toString();
        String key  = "otp:tx:" + txId;

        Map<String, String> fields = new HashMap<>();
        fields.put("identifier", headOtpRequest.identifier().toLowerCase());
        fields.put("channel", headOtpRequest.channel());
        fields.put("hash", hash);
        fields.put("rol", role);
        fields.put("attempts", "0");
        fields.put("used", "0");
        fields.put("resendUntil", String.valueOf(System.currentTimeMillis() + COOLDOWN_SECONDS*1000L));

        redis.opsForHash().putAll(key, fields);
        //redis.expire(key, Duration.ofSeconds(TTL_SECONDS));

        // cooldown para reenviar
        redis.opsForValue().set("otp:cooldown:" + txId, "1", Duration.ofSeconds(COOLDOWN_SECONDS));

        // Enviar por el canal correspondiente
        if ("PHONE".equals(headOtpRequest.channel())) {
            Boolean codeSend = codeSecurityRepo.sendMessage(headOtpRequest.identifier(), code);
        } else {
            Boolean codeSend = codeSecurityRepo.sendMessageEmail(headOtpRequest.identifier(), code);
        }
        return new HEADOtpStarRes(txId, System.currentTimeMillis()/1000 + TTL_SECONDS, COOLDOWN_SECONDS, null);
    }

    @Override
    public HEADOtpVerifyRes verify(HEADVerifyRequest headVerifyRequest) {
        String key = "otp:tx:" + headVerifyRequest.txId();
        if (Boolean.FALSE.equals(redis.hasKey(key))) {
            throw new HEADBadRequestException("Transacción no encontrada o expirada");
        }
        // 1) marcar intento
        Long attempts = redis.opsForHash().increment(key, "attempts", 1L);
        if (attempts != null && attempts > MAX_ATTEMPTS) {
            throw new HEADBadRequestException("Demasiados intentos");
        }
        // 2) verificar ya usado
        String used = (String) redis.opsForHash().get(key, "used");
        if ("1".equals(used)) throw new HEADBadRequestException("OTP ya usada");

        // 3) obtener hash y comparar en JVM (BCrypt)
        String typeChannel = (String) redis.opsForHash().get(key,"channel");
        String identifier = (String) redis.opsForHash().get(key, "identifier");

        boolean ok;
        if ("PHONE".equals(typeChannel)) {
            ok = Boolean.TRUE.equals(codeSecurityRepo.verifySmsCode(identifier, headVerifyRequest.code()));
        } else {
            String hash = (String) redis.opsForHash().get(key, "hash");
            ok = org.mindrot.jbcrypt.BCrypt.checkpw(headVerifyRequest.code(), hash);
        }
        if (!ok) throw new HEADBadRequestException("Código incorrecto. Intenta de nuevo.");

        // 4) marcar usada y acortar TTL (opcional)
        redis.opsForHash().put(key, "used", "1");
        redis.expire(key, Duration.ofSeconds(60));

        // 5) decidir siguiente pantalla (ejemplo)
        String purpose = (String) redis.opsForHash().get(key, "purpose");

        return new HEADOtpVerifyRes(true, headVerifyRequest.code(),typeChannel, headVerifyRequest.isVerifiedOtp(),identifier); // session=null aquí (o créala si quieres login directo)
    }

    @Override
    public HEADOtpStarRes resend(String txId) {
        String key = "otp:tx:" + txId;
        if (Boolean.FALSE.equals(redis.hasKey(key))) {
            throw new HEADBadRequestException("Transacción no encontrada o expirada");
        }
        // respeta cooldown
        String cd = redis.opsForValue().get("otp:cooldown:" + txId);
        if (cd != null) throw new HEADBadRequestException("Espera el cooldown");

        String identifier = (String) redis.opsForHash().get(key, "identifier");
        String channel    = (String) redis.opsForHash().get(key, "channel");

        // genera y actualiza hash
        String code = genCode();
        String hash = org.mindrot.jbcrypt.BCrypt.hashpw(code, org.mindrot.jbcrypt.BCrypt.gensalt(10));
        redis.opsForHash().put(key, "hash", hash);
        redis.opsForHash().put(key, "used", "0");

        // reenvío
        if ("PHONE".equals(channel)) {
            Boolean codeSend = codeSecurityRepo.sendMessage(identifier, code);
        } else {
            Boolean codeSend = codeSecurityRepo.sendMessageEmail(identifier, code);
        }

        redis.opsForValue().set("otp:cooldown:" + txId, "1", Duration.ofSeconds(COOLDOWN_SECONDS));

        Long ttl = redis.getExpire(key); // segundos restantes
        return new HEADOtpStarRes(txId, (System.currentTimeMillis()/1000) + (ttl!=null?ttl:0), COOLDOWN_SECONDS, null);
    }
}

