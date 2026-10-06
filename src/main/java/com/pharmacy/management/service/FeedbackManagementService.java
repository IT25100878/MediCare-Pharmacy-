package com.pharmacy.management.service;

import com.pharmacy.management.dto.FeedbackAnalytics;
import com.pharmacy.management.dto.FeedbackCaseForm;
import com.pharmacy.management.entity.Feedback;
import com.pharmacy.management.repository.AppUserRepository;
import com.pharmacy.management.repository.FeedbackRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class FeedbackManagementService {

    private final FeedbackRepository feedbackRepository;
    private final AppUserRepository appUserRepository;

    public FeedbackManagementService(FeedbackRepository feedbackRepository,
                                     AppUserRepository appUserRepository) {
        this.feedbackRepository = feedbackRepository;
        this.appUserRepository = appUserRepository;
    }
