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

    @Transactional
    public void submit(FeedbackForm form, String currentUserEmail) {
        AppUser currentUser = currentUserEmail == null || currentUserEmail.isBlank()
                ? null
                : appUserRepository.findByEmailIgnoreCase(currentUserEmail).orElse(null);

        Feedback feedback = new Feedback();
        feedback.setUserId(currentUser == null ? null : currentUser.getUserId());
        feedback.setCustomerName(form.getCustomerName().trim());
        feedback.setCustomerPhone(form.getCustomerPhone() == null || form.getCustomerPhone().isBlank()
                ? null : form.getCustomerPhone().trim());
        feedback.setSubject(form.getSubject().trim());
        feedback.setMessage(form.getMessage().trim());
        feedback.setRating(form.getRating());
        feedback.setCategory(form.getCategory());
        feedback.setPriority(form.getPriority());
        feedback.setFeedbackStatus("NEW");
        feedback.setArchived(false);

        feedbackRepository.save(feedback);
    }
}