package com.pharmacy.management.repository;

import com.pharmacy.management.entity.Promotion;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Integer> {

    List<Promotion> findByCampaignCodeContainingIgnoreCaseOrCampaignNameContainingIgnoreCase(
            String campaignCode, String campaignName, Sort sort);

    List<Promotion> findByActiveTrueAndArchivedFalse();
}
