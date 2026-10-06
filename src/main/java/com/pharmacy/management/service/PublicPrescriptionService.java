package com.pharmacy.management.service;

import com.pharmacy.management.dto.PublicPrescriptionRequestForm;
import com.pharmacy.management.entity.Prescription;
import com.pharmacy.management.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Service
public class PublicPrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionFileStorageService fileStorageService;

    public PublicPrescriptionService(PrescriptionRepository prescriptionRepository,
                                     PrescriptionFileStorageService fileStorageService) {
        this.prescriptionRepository = prescriptionRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public String submit(PublicPrescriptionRequestForm form, MultipartFile file) {
        String storedPath = fileStorageService.store(file);
        try {
            Prescription prescription = new Prescription();
            prescription.setPrescriptionNumber(createRequestNumber());
            prescription.setCustomerName(form.getCustomerName().trim());
            prescription.setCustomerPhone(form.getCustomerPhone().trim());
            prescription.setPrescriptionFilePath(storedPath);
            prescription.setPrescriptionStatus("PENDING");
            prescription.setPharmacistUserId(null);
            prescription.setPharmacistNote(null);
            prescription.setReviewedAt(null);
            prescriptionRepository.save(prescription);
            return prescription.getPrescriptionNumber();
        } catch (RuntimeException exception) {
            fileStorageService.deleteQuietly(storedPath);
            throw exception;
        }
    }

    private String createRequestNumber() {
        String day = LocalDate.now().toString().replace("-", "");
        String randomPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        return "WEB-" + day + "-" + randomPart;
    }
}
