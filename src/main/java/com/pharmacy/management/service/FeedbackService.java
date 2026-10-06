package com.pharmacy.management.service;


import com.pharmacy.management.dto.FeedbackForm;
import com.pharmacy.management.entity.Feedback;
import com.pharmacy.management.entity.AppUser;
import com.pharmacy.management.repository.AppUserRepository;
import com.pharmacy.management.repository.FeedbackRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final AppUserRepository appUserRepository;

    public FeedbackService(FeedbackRepository feedbackRepository,
                           AppUserRepository appUserRepository) {
        this.feedbackRepository = feedbackRepository;
        this.appUserRepository = appUserRepository;
    }