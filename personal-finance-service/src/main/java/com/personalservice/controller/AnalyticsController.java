package com.personalservice.controller;

import com.personalservice.dto.CalendarDayExpenseResponse;
import com.personalservice.dto.MonthlySummaryResponse;
import com.personalservice.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/personal")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    public ResponseEntity<MonthlySummaryResponse> getMonthlySummary(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam String month) {
        return ResponseEntity.ok(analyticsService.getMonthlySummary(userId, month));
    }

    @GetMapping("/calendar")
    public ResponseEntity<List<CalendarDayExpenseResponse>> getCalendarExpenses(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam String month) {
        return ResponseEntity.ok(analyticsService.getCalendarExpenses(userId, month));
    }
}
