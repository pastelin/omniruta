package com.HealthAdvanced.healthAdvanced.HEHOGlobalExceptions;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class HEHOGlobalExceptionHandler {

    @ExceptionHandler(MultipartException.class)
    public String handlerMultipar(MultipartException e, RedirectAttributes attributes) {
        attributes.addFlashAttribute("message", e.getCause().getMessage());
        return "redirect:/status";
    }

}
