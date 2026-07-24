package com.ariscend.backend.controller;

import com.ariscend.backend.dto.completion.CompleteHabitRequest;
import com.ariscend.backend.dto.completion.HabitCompletionResponse;
import com.ariscend.backend.dto.habit.CreateHabitRequest;
import com.ariscend.backend.dto.habit.HabitResponse;
import com.ariscend.backend.service.HabitService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/habits")
public class HabitController {

    private final HabitService habitService;

    public HabitController(HabitService habitService) {
        this.habitService = habitService;
    }

    @GetMapping
    public List<HabitResponse> getAllByUser(@RequestParam Long userId) {
        return habitService.getAllByUser(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HabitResponse create(@RequestBody CreateHabitRequest request) {
        return habitService.create(request);
    }

    @PostMapping("/{habitId}/complete")
    @ResponseStatus(HttpStatus.CREATED)
    public HabitCompletionResponse complete(
            @PathVariable Long habitId,
            @RequestBody CompleteHabitRequest request
    ) {
        return habitService.complete(habitId, request);
    }

    @GetMapping("/{habitId}/completions")
    public List<HabitCompletionResponse> getCompletions(@PathVariable Long habitId) {
        return habitService.getCompletions(habitId);
    }

    @DeleteMapping("/{habitId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long habitId) {
        habitService.deactivate(habitId);
    }
}