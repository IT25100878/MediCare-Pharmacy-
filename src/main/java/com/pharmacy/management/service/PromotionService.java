package com.pharmacy.management.service;

import com.pharmacy.management.dto.PromotionDiscount;
import com.pharmacy.management.entity.Medicine;
import com.pharmacy.management.entity.PharmacyBranch;
import com.pharmacy.management.entity.Promotion;
import com.pharmacy.management.entity.PromotionRedemption;
import com.pharmacy.management.repository.PromotionRedemptionRepository;
import com.pharmacy.management.repository.PromotionRepository;
import com.pharmacy.management.repository.PermanentDeletionRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionRedemptionRepository promotionRedemptionRepository;
    private final CurrentUserService currentUserService;
    private final PermanentDeletionRepository permanentDeletionRepository;

    public PromotionService(PromotionRepository promotionRepository,
                            PromotionRedemptionRepository promotionRedemptionRepository,
                            CurrentUserService currentUserService,
                            PermanentDeletionRepository permanentDeletionRepository) {
        this.promotionRepository = promotionRepository;
        this.promotionRedemptionRepository = promotionRedemptionRepository;
        this.currentUserService = currentUserService;
        this.permanentDeletionRepository = permanentDeletionRepository;
    }

    public List<Promotion> findAll(String keyword) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if (keyword == null || keyword.isBlank()) {
            return promotionRepository.findAll(sort);
        }
        String search = keyword.trim();
        return promotionRepository.findByCampaignCodeContainingIgnoreCaseOrCampaignNameContainingIgnoreCase(
                search, search, sort
        );
    }

    public Promotion findById(Integer promotionId) {
        return promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Promotion campaign was not found."));
    }

    @Transactional
    public void create(Promotion form, String currentUserEmail) {
        validate(form);
        form.setPromotionId(null);
        form.setCampaignCode(form.getCampaignCode().trim().toUpperCase(Locale.ROOT));
        form.setCampaignName(form.getCampaignName().trim());
        form.setDescription(trimToNull(form.getDescription()));
        form.setCategory(trimToNull(form.getCategory()));
        form.setArchived(false);
        form.setCreatedByUserId(currentUserService.getUserId(currentUserEmail));
        form.setUpdatedAt(LocalDateTime.now());
        promotionRepository.save(form);
    }

    @Transactional
    public void update(Integer promotionId, Promotion form) {
        validate(form);
        Promotion existing = findById(promotionId);
        existing.setCampaignCode(form.getCampaignCode().trim().toUpperCase(Locale.ROOT));
        existing.setCampaignName(form.getCampaignName().trim());
        existing.setDescription(trimToNull(form.getDescription()));
        existing.setDiscountType(form.getDiscountType());
        existing.setDiscountValue(form.getDiscountValue());
        existing.setMinimumOrderAmount(form.getMinimumOrderAmount());
        existing.setMedicineId(form.getMedicineId());
        existing.setCategory(trimToNull(form.getCategory()));
        existing.setBranchId(form.getBranchId());
        existing.setStartDate(form.getStartDate());
        existing.setEndDate(form.getEndDate());
        existing.setActive(form.isActive());
        existing.setUpdatedAt(LocalDateTime.now());
        promotionRepository.save(existing);
    }

    @Transactional
    public void delete(Integer promotionId) {
        findById(promotionId);
        permanentDeletionRepository.deletePromotion(promotionId);
    }

    public PromotionDiscount calculateBestDiscount(Medicine medicine,
                                                   PharmacyBranch branch,
                                                   BigDecimal grossAmount) {
        if (medicine == null || branch == null || grossAmount == null || grossAmount.signum() <= 0) {
            return PromotionDiscount.none();
        }
        LocalDate today = LocalDate.now();
        return promotionRepository.findByActiveTrueAndArchivedFalse().stream()
                .filter(promotion -> isLive(promotion, today))
                .filter(promotion -> matchesMedicine(promotion, medicine))
                .filter(promotion -> promotion.getBranchId() == null
                        || promotion.getBranchId().equals(branch.getBranchId()))
                .filter(promotion -> grossAmount.compareTo(nonNullMoney(promotion.getMinimumOrderAmount())) >= 0)
                .map(promotion -> new PromotionDiscount(promotion, calculateDiscount(promotion, grossAmount)))
                .filter(discount -> discount.discountAmount().signum() > 0)
                .max(Comparator.comparing(PromotionDiscount::discountAmount))
                .orElseGet(PromotionDiscount::none);
    }

    @Transactional
    public void recordRedemption(PromotionDiscount discount, Integer orderId) {
        if (discount == null || discount.promotion() == null || discount.discountAmount().signum() <= 0) {
            return;
        }
        PromotionRedemption redemption = new PromotionRedemption();
        redemption.setPromotionId(discount.promotion().getPromotionId());
        redemption.setOrderId(orderId);
        redemption.setDiscountAmount(discount.discountAmount());
        promotionRedemptionRepository.save(redemption);
    }

    private void validate(Promotion promotion) {
        if (promotion.getStartDate() != null && promotion.getEndDate() != null
                && promotion.getEndDate().isBefore(promotion.getStartDate())) {
            throw new IllegalArgumentException("Campaign end date must be on or after its start date.");
        }
        if ("PERCENT".equals(promotion.getDiscountType())
                && promotion.getDiscountValue() != null
                && promotion.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Percentage discount cannot be more than 100%.");
        }
        if (promotion.getMedicineId() != null && promotion.getCategory() != null && !promotion.getCategory().isBlank()) {
            throw new IllegalArgumentException("Choose either one medicine or one category for a targeted campaign.");
        }
    }

    private boolean isLive(Promotion promotion, LocalDate today) {
        return promotion.getStartDate() != null
                && promotion.getEndDate() != null
                && !today.isBefore(promotion.getStartDate())
                && !today.isAfter(promotion.getEndDate());
    }

    private boolean matchesMedicine(Promotion promotion, Medicine medicine) {
        if (promotion.getMedicineId() != null) {
            return promotion.getMedicineId().equals(medicine.getMedicineId());
        }
        if (promotion.getCategory() == null || promotion.getCategory().isBlank()) {
            return true;
        }
        return medicine.getCategory() != null
                && promotion.getCategory().trim().equalsIgnoreCase(medicine.getCategory().trim());
    }

    private BigDecimal calculateDiscount(Promotion promotion, BigDecimal grossAmount) {
        BigDecimal rawDiscount = "PERCENT".equals(promotion.getDiscountType())
                ? grossAmount.multiply(promotion.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : promotion.getDiscountValue();
        return rawDiscount.min(grossAmount).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal nonNullMoney(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
