package com.moviebooking.ticket.service;

import java.security.MessageDigest;
import java.util.Base64;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class QrCodeService {

    public String generateQrCode(String ticketId, String bookingId) {
        try {
            // Create a unique string combining ticket and booking info
            String data = ticketId + ":" + bookingId + ":" + System.currentTimeMillis();
            
            // Generate SHA-256 hash
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes("UTF-8"));
            
            // Encode to Base64 and return first 32 characters for QR code
            String qrCode = Base64.getEncoder().encodeToString(hash).substring(0, 32);
            
            log.info("Generated QR code for ticket: {}", ticketId);
            return qrCode;
            
        } catch (Exception e) {
            log.error("Error generating QR code for ticket: {}", ticketId, e);
            // Fallback to simple hash
            return "QR" + Math.abs((ticketId + bookingId).hashCode());
        }
    }

    public boolean validateQrCode(String qrCode, String ticketId) {
        // In a real implementation, this would validate the QR code
        // For now, we'll just check if it's not empty
        return qrCode != null && !qrCode.trim().isEmpty();
    }
}