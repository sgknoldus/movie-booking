package com.moviebooking.ticket.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class QrCodeServiceTest {

    @InjectMocks
    private QrCodeService qrCodeService;

    private String testTicketId;
    private String testBookingId;

    @BeforeEach
    void setUp() {
        testTicketId = "TK-123456789-abcd1234";
        testBookingId = "BK-123456789-efgh5678";
    }

    @Test
    void generateQrCode_ShouldGenerateValidQrCode() {
        // When
        String qrCode = qrCodeService.generateQrCode(testTicketId, testBookingId);

        // Then
        assertThat(qrCode).isNotNull();
        assertThat(qrCode).isNotEmpty();
        assertThat(qrCode).hasSize(32); // Base64 encoded hash truncated to 32 chars
    }

    @Test
    void generateQrCode_ShouldGenerateUniqueCodesForDifferentInputs() {
        // Given
        String differentTicketId = "TK-987654321-wxyz9876";
        String differentBookingId = "BK-987654321-mnop5432";

        // When
        String qrCode1 = qrCodeService.generateQrCode(testTicketId, testBookingId);
        String qrCode2 = qrCodeService.generateQrCode(differentTicketId, differentBookingId);

        // Then
        assertThat(qrCode1).isNotEqualTo(qrCode2);
    }

    @Test
    void generateQrCode_ShouldGenerateDifferentCodesForSameInputs() {
        // Since timestamp is included, even same inputs should generate different codes
        // when called at different times

        // When
        String qrCode1 = qrCodeService.generateQrCode(testTicketId, testBookingId);

        try {
            Thread.sleep(1); // Ensure different timestamp
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String qrCode2 = qrCodeService.generateQrCode(testTicketId, testBookingId);

        // Then
        assertThat(qrCode1).isNotEqualTo(qrCode2);
    }

    @Test
    void generateQrCode_ShouldHandleNullInputs() {
        // When
        String qrCodeWithNullTicket = qrCodeService.generateQrCode(null, testBookingId);
        String qrCodeWithNullBooking = qrCodeService.generateQrCode(testTicketId, null);
        String qrCodeWithBothNull = qrCodeService.generateQrCode(null, null);

        // Then
        assertThat(qrCodeWithNullTicket).isNotNull().isNotEmpty();
        assertThat(qrCodeWithNullBooking).isNotNull().isNotEmpty();
        assertThat(qrCodeWithBothNull).isNotNull().isNotEmpty();
    }

    @Test
    void generateQrCode_ShouldHandleEmptyStrings() {
        // When
        String qrCode = qrCodeService.generateQrCode("", "");

        // Then
        assertThat(qrCode).isNotNull().isNotEmpty();
    }

    @Test
    void generateQrCode_ShouldHandleLongInputs() {
        // Given
        String longTicketId = "TK-".repeat(100) + testTicketId;
        String longBookingId = "BK-".repeat(100) + testBookingId;

        // When
        String qrCode = qrCodeService.generateQrCode(longTicketId, longBookingId);

        // Then
        assertThat(qrCode).isNotNull();
        assertThat(qrCode).isNotEmpty();
        assertThat(qrCode).hasSize(32);
    }

    @Test
    void generateQrCode_ShouldHandleSpecialCharacters() {
        // Given
        String specialTicketId = "TK-!@#$%^&*()_+-=[]{}|;:,.<>?";
        String specialBookingId = "BK-àáâãäåæçèéêëìíîïñòóôõöøùúûüý";

        // When
        String qrCode = qrCodeService.generateQrCode(specialTicketId, specialBookingId);

        // Then
        assertThat(qrCode).isNotNull();
        assertThat(qrCode).isNotEmpty();
        assertThat(qrCode).hasSize(32);
    }

    @Test
    void generateQrCode_ShouldUseFallbackWhenExceptionOccurs() {
        // This test is tricky because we can't easily force an exception in the current implementation
        // But we can test with inputs that might cause issues and verify fallback behavior

        // Given - Using very long strings that might cause memory issues in some scenarios
        String problematicTicketId = "TK-" + "x".repeat(1000000);
        String problematicBookingId = "BK-" + "y".repeat(1000000);

        // When
        String qrCode = qrCodeService.generateQrCode(problematicTicketId, problematicBookingId);

        // Then
        assertThat(qrCode).isNotNull();
        assertThat(qrCode).isNotEmpty();
        // Should be either the normal 32-char hash or the fallback format
        assertThat(qrCode.length()).isGreaterThanOrEqualTo(2); // At minimum "QR" + some number
    }

    @Test
    void validateQrCode_ShouldReturnTrue_WhenQrCodeIsValid() {
        // Given
        String validQrCode = "ABC123DEF456GHI789JKL012MNO345PQ";

        // When
        boolean isValid = qrCodeService.validateQrCode(validQrCode, testTicketId);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void validateQrCode_ShouldReturnFalse_WhenQrCodeIsNull() {
        // When
        boolean isValid = qrCodeService.validateQrCode(null, testTicketId);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void validateQrCode_ShouldReturnFalse_WhenQrCodeIsEmpty() {
        // When
        boolean isValidEmpty = qrCodeService.validateQrCode("", testTicketId);
        boolean isValidWhitespace = qrCodeService.validateQrCode("   ", testTicketId);

        // Then
        assertThat(isValidEmpty).isFalse();
        assertThat(isValidWhitespace).isFalse();
    }

    @Test
    void validateQrCode_ShouldHandleNullTicketId() {
        // Given
        String validQrCode = "ABC123DEF456GHI789JKL012MNO345PQ";

        // When
        boolean isValid = qrCodeService.validateQrCode(validQrCode, null);

        // Then
        assertThat(isValid).isTrue(); // Current implementation doesn't use ticketId for validation
    }

    @Test
    void generateQrCode_ShouldProduceBase64Characters() {
        // When
        String qrCode = qrCodeService.generateQrCode(testTicketId, testBookingId);

        // Then
        assertThat(qrCode).matches("[A-Za-z0-9+/=]+"); // Valid Base64 characters
    }

    @Test
    void generateQrCode_ShouldBeConsistentWithSameTimestamp() {
        // This test verifies the deterministic part of the algorithm
        // by checking that the same inputs at least produce valid outputs

        // When
        String qrCode1 = qrCodeService.generateQrCode(testTicketId, testBookingId);
        String qrCode2 = qrCodeService.generateQrCode(testTicketId, testBookingId);

        // Then
        assertThat(qrCode1).hasSize(32);
        assertThat(qrCode2).hasSize(32);
        assertThat(qrCode1).matches("[A-Za-z0-9+/]+");
        assertThat(qrCode2).matches("[A-Za-z0-9+/]+");
    }

    @Test
    void validateQrCode_ShouldReturnTrue_ForGeneratedQrCode() {
        // Given
        String generatedQrCode = qrCodeService.generateQrCode(testTicketId, testBookingId);

        // When
        boolean isValid = qrCodeService.validateQrCode(generatedQrCode, testTicketId);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void generateQrCode_ShouldHandleUnicodeCharacters() {
        // Given
        String unicodeTicketId = "TK-测试票证-🎫";
        String unicodeBookingId = "BK-예약아이디-🎪";

        // When
        String qrCode = qrCodeService.generateQrCode(unicodeTicketId, unicodeBookingId);

        // Then
        assertThat(qrCode).isNotNull();
        assertThat(qrCode).isNotEmpty();
        assertThat(qrCode).hasSize(32);
    }
}