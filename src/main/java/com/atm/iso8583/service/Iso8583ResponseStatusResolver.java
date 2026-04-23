package com.atm.iso8583.service;

import com.atm.iso8583.model.Iso8583Response;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class Iso8583ResponseStatusResolver {

    @NonNull
    public HttpStatus resolve(Iso8583Response response) {
        if (response == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        if ("ERROR".equalsIgnoreCase(response.getStatus())) {
            String error = response.getErrorMessage();
            if (error != null && error.toLowerCase(Locale.ROOT).contains("timeout")) {
                return HttpStatus.GATEWAY_TIMEOUT;
            }
            return HttpStatus.SERVICE_UNAVAILABLE;
        }

        String responseCode = response.getResponseCode();
        if (responseCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return switch (responseCode) {
            case "00" -> HttpStatus.OK;
            case "05", "14", "41", "43" -> HttpStatus.FORBIDDEN;
            case "51", "61" -> HttpStatus.PAYMENT_REQUIRED;
            case "91", "96" -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
