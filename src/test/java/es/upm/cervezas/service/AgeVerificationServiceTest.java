package es.upm.cervezas.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class AgeVerificationServiceTest {

    private final AgeVerificationService service = new AgeVerificationService();

    @Test
    void allowsAdults() {
        LocalDate adult = LocalDate.now().minusYears(20);
        assertDoesNotThrow(() -> service.verifyOrThrow(adult));
    }

    @Test
    void rejectsMinors() {
        LocalDate minor = LocalDate.now().minusYears(17);
        assertThrows(IllegalArgumentException.class, () -> service.verifyOrThrow(minor));
    }
}
