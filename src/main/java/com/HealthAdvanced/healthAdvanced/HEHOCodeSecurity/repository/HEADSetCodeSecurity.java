package com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.repository;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADErrorMessageClient;
import com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.HEADCodeSecurityInterfaces.HEADCodeSecurityInputRepository;
import com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.Model.HEADRequest.HEADModelEmail;
import com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.Model.HEADRequest.TelnyxSendOtpRequest;
import com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.Model.HEADRequest.TelnyxVerifyOtpRequest;
import com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.Model.HEADResponse.HEADCodeSecurityResponse;
import com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.Model.HEADResponse.TelnyxSendOtpResponse;
import com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.Model.HEADResponse.TelnyxVerifyOtpResponse;
import com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.Model.propertiesModel.TelnyxProperties;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class HEADSetCodeSecurity implements HEADCodeSecurityInputRepository {

    private final JavaMailSender mailSender;
    private final WebClient telnyxWebClient;
    private final TelnyxProperties telnyxProperties;
    @Override
    public Boolean sendMessage(String numberPhone, String Code) {
        try {
            String normalizedPhone = normalizeMxPhone(numberPhone);

            TelnyxSendOtpRequest request = new TelnyxSendOtpRequest(
                    normalizedPhone,
                    telnyxProperties.getVerifyProfileId()
            );

            TelnyxSendOtpResponse response = telnyxWebClient.post()
                    .uri("/verifications/sms")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(TelnyxSendOtpResponse.class)
                    .block();

            return response != null
                    && response.data() != null
                    && "pending".equalsIgnoreCase(response.data().status());

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Boolean verifySmsCode(String numberPhone, String Code) {
        try {
            String normalizedPhone = normalizeMxPhone(numberPhone);

            TelnyxVerifyOtpRequest request = new TelnyxVerifyOtpRequest(
                    Code,
                    telnyxProperties.getVerifyProfileId()
            );

            TelnyxVerifyOtpResponse response = telnyxWebClient.post()
                    .uri("/verifications/by_phone_number/{phone}/actions/verify", normalizedPhone)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(TelnyxVerifyOtpResponse.class)
                    .block();

            return response != null
                    && response.data() != null
                    && "accepted".equalsIgnoreCase(response.data().response_code());

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Boolean sendMessageEmail(String email, String code) {
        return sendMessageEmail(email, null, code);
    }

    public Boolean sendMessageEmail(String email, String userName, String code) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            int expirationMinutes = 5;
            boolean hasUserName = userName != null && !userName.trim().isEmpty();
            String safeName = hasUserName ? userName.trim() : "";

            String greetingHtml = hasUserName
                    ? "<div style=\"font-size:15px; line-height:24px; color:#475467;\">Hola, <strong>" + safeName + "</strong>.</div>"
                    : "<div style=\"font-size:15px; line-height:24px; color:#475467;\">Hola.</div>";

            helper.setFrom(new InternetAddress("no-reply@docarya.com.mx", "Docarya"));
            helper.setTo(email);
            helper.setSubject("Código de verificación de tu cuenta Docarya");

            String htmlContent = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <meta http-equiv="X-UA-Compatible" content="IE=edge">
                    <title>Docarya - Código de verificación</title>
                </head>
                <body style="margin:0; padding:0; background-color:#eef4fb; font-family:Arial, Helvetica, sans-serif;">

                    <div style="display:none; max-height:0; overflow:hidden; opacity:0; color:transparent;">
                        Tu código de verificación de Docarya es %s
                    </div>

                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="background-color:#eef4fb; margin:0; padding:24px 0;">
                        <tr>
                            <td align="center">

                                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0"
                                       style="max-width:620px; background-color:#ffffff; border-radius:20px; overflow:hidden; box-shadow:0 10px 35px rgba(23,113,201,0.10);">

                                    <tr>
                                        <td style="background:linear-gradient(135deg,#1771C9 0%%,#2E90FA 100%%); padding:28px 32px; text-align:center;">
                                            <div style="font-size:28px; line-height:28px; font-weight:700; color:#ffffff;">
                                                Docarya
                                            </div>
                                            <div style="margin-top:8px; font-size:14px; line-height:20px; color:#dbeafe;">
                                                Seguridad y acceso confiable
                                            </div>
                                        </td>
                                    </tr>

                                    <tr>
                                        <td style="padding:36px 32px 12px 32px;">
                                            <div style="font-size:24px; line-height:32px; font-weight:700; color:#0f172a; margin-bottom:10px;">
                                                Código de verificación
                                            </div>

                                            %s

                                            <div style="font-size:15px; line-height:24px; color:#475467; margin-top:10px;">
                                                Recibimos una solicitud para verificar tu acceso en <strong>Docarya</strong>.
                                                Usa el siguiente código para continuar:
                                            </div>
                                        </td>
                                    </tr>

                                    <tr>
                                        <td align="center" style="padding:18px 32px 10px 32px;">
                                            <table role="presentation" cellspacing="0" cellpadding="0" border="0">
                                                <tr>
                                                    <td style="background-color:#f8fbff; border:2px solid #bfdcff; border-radius:16px; padding:18px 30px;">
                                                        <div style="font-size:34px; line-height:40px; font-weight:700; letter-spacing:8px; color:#1771C9; text-align:center;">
                                                            %s
                                                        </div>
                                                    </td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>

                                    <tr>
                                        <td style="padding:12px 32px 8px 32px; text-align:center;">
                                            <div style="font-size:14px; line-height:22px; color:#667085;">
                                                Este código vence en <strong>%d minutos</strong>.
                                            </div>
                                        </td>
                                    </tr>

                                    <tr>
                                        <td style="padding:20px 32px 8px 32px;">
                                            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0"
                                                   style="background-color:#f9fafb; border:1px solid #e5e7eb; border-radius:14px;">
                                                <tr>
                                                    <td style="padding:16px 18px;">
                                                        <div style="font-size:14px; line-height:22px; color:#475467;">
                                                            <strong style="color:#0f172a;">Importante:</strong>
                                                            nunca compartas este código con nadie.
                                                        </div>
                                                    </td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>

                                    <tr>
                                        <td style="padding:10px 32px 0 32px;">
                                            <div style="font-size:14px; line-height:22px; color:#667085;">
                                                Si no solicitaste este acceso, puedes ignorar este mensaje de forma segura.
                                            </div>
                                        </td>
                                    </tr>

                                    <tr>
                                        <td style="padding:28px 32px 18px 32px;">
                                            <div style="height:1px; background-color:#e5e7eb;"></div>
                                        </td>
                                    </tr>

                                    <tr>
                                        <td style="padding:0 32px 30px 32px; text-align:center;">
                                            <div style="font-size:13px; line-height:20px; color:#98a2b3;">
                                                Este es un correo automático. Por favor, no respondas a este mensaje.
                                            </div>
                                            <div style="margin-top:8px; font-size:12px; line-height:18px; color:#98a2b3;">
                                                © 2026 Docarya. Todos los derechos reservados.
                                            </div>
                                        </td>
                                    </tr>

                                </table>

                            </td>
                        </tr>
                    </table>

                </body>
                </html>
                """.formatted(code, greetingHtml, code, expirationMinutes);

            helper.setText(htmlContent, true);
            mailSender.send(message);
            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }


    private String normalizeMxPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new HEADBadRequestException("El numero de telefono es requerido");
        }

        String digits = phone.replaceAll("\\D", "");

        if (digits.startsWith("52") && digits.length() == 12) {
            return "+" + digits;
        }

        if (digits.length() == 10) {
            return "+52" + digits;
        }

        if (digits.startsWith("521") && digits.length() == 13) {
            return "+52" + digits.substring(3);
        }

        throw new HEADBadRequestException("Formato de teléfono inválido");
    }
}
