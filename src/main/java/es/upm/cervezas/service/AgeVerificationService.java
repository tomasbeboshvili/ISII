package es.upm.cervezas.service;

import java.time.LocalDate;
import java.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AgeVerificationService {

    private static final int LEGAL_AGE = 18;
    private static final Logger log = LoggerFactory.getLogger(AgeVerificationService.class);

    public void verifyOrThrow(LocalDate birthDate) {
        if (birthDate == null) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria");
        }

        int years = Period.between(birthDate, LocalDate.now()).getYears();
        log.debug("Verificando edad: {} años", years);
        if (years < LEGAL_AGE) {
            throw new IllegalArgumentException("Debes ser mayor de edad para crear una cuenta");
        }
    }
}
