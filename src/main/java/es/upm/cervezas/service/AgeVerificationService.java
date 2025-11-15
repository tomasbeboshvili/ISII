package es.upm.cervezas.service;

import java.time.LocalDate;
import java.time.Period;
import org.springframework.stereotype.Service;

@Service
public class AgeVerificationService {

    private static final int LEGAL_AGE = 18;

    public void verifyOrThrow(LocalDate birthDate) {
        if (birthDate == null) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria");
        }

        int years = Period.between(birthDate, LocalDate.now()).getYears();
        if (years < LEGAL_AGE) {
            throw new IllegalArgumentException("Debes ser mayor de edad para crear una cuenta");
        }
    }
}
