package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADErrors.handler;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADErrors.dto.HEADApiError;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADErrors.filters.HEADCorrelationIdFilter;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADErrors.helpers.HEADFolioUtils;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class HEADGlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(HEADBadRequestException.class)
    public ResponseEntity<HEADApiError> handleBadRequest(
            HEADBadRequestException ex,
            HttpServletRequest request
    ) {
        String folio = HEADFolioUtils.getFolio(request);

        log.warn("HEADBadRequestException folio={} path={} detail={}",
                folio, request.getRequestURI(), ex.getMessage());

        HEADApiError body = new HEADApiError(
                OffsetDateTime.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                "BUSINESS_ERROR",
                ex.getMessage(),
                request.getRequestURI(),
                folio,
                List.of()
        );

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HEADBusinessException.class)
    public ResponseEntity<HEADApiError> handleBusiness(
            HEADBusinessException ex,
            HttpServletRequest request
    ) {
        String folio = HEADFolioUtils.getFolio(request);

        log.warn("HEADBusinessException folio={} path={} detail={}",
                folio, request.getRequestURI(), ex.getMessage());

        HEADApiError body = new HEADApiError(
                OffsetDateTime.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                "No se pudo procesar la solicitud. Comparte el folio con soporte.",
                request.getRequestURI(),
                folio,
                List.of()
        );

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<HEADApiError> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        String folio = HEADFolioUtils.getFolio(request);

        List<String> details = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();

        HEADApiError body = new HEADApiError(
                OffsetDateTime.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                "Error de validación",
                request.getRequestURI(),
                folio,
                details
        );

        log.warn("ConstraintViolation folio={} path={} details={}",
                folio, request.getRequestURI(), details);

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<HEADApiError> handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {
        String folio = HEADFolioUtils.getFolio(request);

        log.error("Unhandled exception folio={} path={}", folio, request.getRequestURI(), ex);

        HEADApiError body = new HEADApiError(
                OffsetDateTime.now().toString(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "Ocurrió un error interno. Comparte el folio con soporte.",
                request.getRequestURI(),
                folio,
                List.of()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        String folio = HEADFolioUtils.getFolio(((ServletWebRequest) request).getRequest());

        List<String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .toList();

        HEADApiError body = new HEADApiError(
                OffsetDateTime.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                "Datos inválidos",
                path,
                folio,
                details
        );

        log.warn("MethodArgumentNotValid folio={} path={} details={}", folio, path, details);

        return ResponseEntity.badRequest().body(body);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        String folio = HEADFolioUtils.getFolio( ((ServletWebRequest) request).getRequest());

        HEADApiError body = new HEADApiError(
                OffsetDateTime.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                "MALFORMED_JSON",
                "El body del request es inválido o está mal formado",
                path,
                folio,
                List.of()
        );

        log.warn("HttpMessageNotReadable folio={} path={}", folio, path, ex);

        return ResponseEntity.badRequest().body(body);
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " +
                (fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "valor inválido");
    }
}