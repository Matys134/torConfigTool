package com.school.torconfigtool;

import com.school.torconfigtool.RelayOperationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * This class is a global exception handler for the application.
 * It uses the @ControllerAdvice annotation to handle exceptions globally.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * This method handles RelayOperationExceptions.
     * It takes the exception and a Model as parameters, adds an error message to the model,
     * and returns a view name.
     *
     * @param ex the exception that occurred
     * @param model the model to which the error message will be added
     * @return the name of the view that will be displayed
     */
    @ExceptionHandler(RelayOperationException.class)
    public String handleRelayOperationException(RelayOperationException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "relay-operations";
    }
}