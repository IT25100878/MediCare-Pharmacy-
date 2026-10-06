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

    public List<Feedback> findCases(String keyword, String status, boolean includeArchived) {
        List<Feedback> cases = includeArchived
                ? feedbackRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                : feedbackRepository.findByArchivedFalse(Sort.by(Sort.Direction.DESC, "createdAt"));
        String search = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        String selectedStatus = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        return cases.stream()
                .filter(item -> selectedStatus.isBlank() || selectedStatus.equals(item.getFeedbackStatus()))
                .filter(item -> search.isBlank()
                        || contains(item.getCustomerName(), search)
                        || contains(item.getSubject(), search)
                        || contains(item.getMessage(), search)
                        || contains(item.getCategory(), search))
                .toList();
    }

    public Feedback findById(Integer feedbackId) {
        return feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Feedback case was not found."));
    }

    @Transactional
    public void update(Integer feedbackId, FeedbackCaseForm form) {
        Feedback feedback = findById(feedbackId);
        if (form.getAssignedToUserId() != null && !appUserRepository.existsById(form.getAssignedToUserId())) {
            throw new IllegalArgumentException("The selected case owner no longer exists.");
        }
        feedback.setFeedbackStatus(form.getFeedbackStatus());
        feedback.setCategory(form.getCategory());
        feedback.setPriority(form.getPriority());
        feedback.setAssignedToUserId(form.getAssignedToUserId());
        feedback.setAdminResponse(trimToNull(form.getAdminResponse()));
        feedback.setClosedAt("CLOSED".equals(form.getFeedbackStatus()) ? LocalDateTime.now() : null);
        feedback.setUpdatedAt(LocalDateTime.now());
        feedbackRepository.save(feedback);
    }

    @Transactional
    public void archive(Integer feedbackId) {
        Feedback feedback = findById(feedbackId);
        feedback.setArchived(true);
        feedback.setUpdatedAt(LocalDateTime.now());
        feedbackRepository.save(feedback);
    }


    public FeedbackAnalytics analytics() {
        List<Feedback> cases = feedbackRepository.findByArchivedFalse(Sort.by(Sort.Direction.DESC, "createdAt"));
        long newCases = cases.stream().filter(item -> "NEW".equals(item.getFeedbackStatus())).count();
        long inProgress = cases.stream().filter(item -> "IN_PROGRESS".equals(item.getFeedbackStatus())).count();
        long resolved = cases.stream().filter(item -> "RESOLVED".equals(item.getFeedbackStatus())).count();
        long closed = cases.stream().filter(item -> "CLOSED".equals(item.getFeedbackStatus())).count();
        long urgent = cases.stream().filter(item -> "URGENT".equals(item.getPriority())).count();
        double average = cases.stream().map(Feedback::getRating).filter(value -> value != null)
                .mapToInt(Integer::intValue).average().orElse(0.0);
        return new FeedbackAnalytics(cases.size(), newCases, inProgress, resolved, closed, urgent, average);
    }



