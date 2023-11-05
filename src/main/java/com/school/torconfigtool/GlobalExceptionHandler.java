package com.school.torconfigtool;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RelayOperationException.class) // Accessible as it's now in the same package
    public String handleRelayOperationException(RelayOperationException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "relay-operations";
    }
}
