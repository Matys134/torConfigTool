package com.school.torconfigtool;

import com.school.torconfigtool.controllers.api.RelayOperationsController;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RelayOperationsController.RelayOperationException.class)
    public String handeRelayOperationException(RelayOperationsController.RelayOperationException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "relay-operations";
    }
}
