package com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.implementations;

import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.dto.HEADNotificationCommand;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.dto.HEADNotificationText;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.interfaces.HEADNotificationTextComposer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HEADNotificationTextComposerImpl implements HEADNotificationTextComposer {

    private final MessageSource messageSource;
    private static final Locale DEFAULT_LOCALE = Locale.forLanguageTag("es-MX");


    @Override
    public HEADNotificationText compose(HEADNotificationCommand cmd) {

        Locale locale = resolveLocale(cmd);

        String baseKey = cmd.templateCode(); // ahora es templateKey, ej: "job.en_route.client"

        String titleTpl = getMsg(baseKey + ".title", locale);
        String bodyTpl  = getMsg(baseKey + ".body", locale);

        String title = applyParams(titleTpl, cmd.params());
        String body  = applyParams(bodyTpl, cmd.params());

        if (title == null || title.isBlank()) title = "Notificación";
        if (body == null) body = "";

        return new HEADNotificationText(title, body);
    }

    private String getMsg(String key, Locale locale) {
        try {
            return messageSource.getMessage(key, null, locale);
        } catch (Exception ignore) {
            return null;
        }
    }

    /*private Locale resolveLocale(HEADNotificationCommand c) {
        return Optional.ofNullable(c.locale())
                .orElse(Locale.forLanguageTag("es-MX"));
                //.orElseGet(() -> Optional.ofNullable(LocaleContextHolder.getLocale()).orElse(Locale.getDefault()));
    }*/

    private Locale resolveLocale(HEADNotificationCommand c) {
        Locale locale = c.locale();
        if (locale == null) return DEFAULT_LOCALE;

        if ("es".equalsIgnoreCase(locale.getLanguage())) {
            return DEFAULT_LOCALE;
        }

        return locale;
    }

    private String applyParams(String template, Map<String, Object> params) {
        String base = Optional.ofNullable(template).orElse("");
        Map<String, Object> safeParams = Optional.ofNullable(params).orElseGet(Collections::emptyMap);

        return safeParams.entrySet().stream().reduce(
                base,
                (acc, e) -> acc.replace("{" + e.getKey() + "}", Objects.toString(e.getValue(), "")),
                (s1, s2) -> s1
        );
    }
}


