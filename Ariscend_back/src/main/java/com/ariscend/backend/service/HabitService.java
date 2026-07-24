package com.ariscend.backend.service;

import com.ariscend.backend.dto.completion.CompleteHabitRequest;
import com.ariscend.backend.dto.completion.HabitCompletionResponse;
import com.ariscend.backend.dto.habit.CreateHabitRequest;
import com.ariscend.backend.dto.habit.HabitResponse;
import com.ariscend.backend.entity.AppUser;
import com.ariscend.backend.entity.Habit;
import com.ariscend.backend.entity.HabitCompletion;
import com.ariscend.backend.exception.ResourceNotFoundException;
import com.ariscend.backend.repository.AppUserRepository;
import com.ariscend.backend.repository.HabitCompletionRepository;
import com.ariscend.backend.repository.HabitRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class HabitService {

    private final HabitRepository habitRepository;
    private final AppUserRepository appUserRepository;
    private final HabitCompletionRepository habitCompletionRepository;

    public HabitService(
            HabitRepository habitRepository,
            AppUserRepository appUserRepository,
            HabitCompletionRepository habitCompletionRepository
    ) {
        this.habitRepository = habitRepository;
        this.appUserRepository = appUserRepository;
        this.habitCompletionRepository = habitCompletionRepository;
    }

    public List<HabitResponse> getAllByUser(Long userId) {
        return habitRepository.findByUserIdAndActiveTrueOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public HabitResponse create(CreateHabitRequest request) {
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("El usuario es obligatorio.");
        }

        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre del hábito es obligatorio.");
        }

        AppUser user = appUserRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        Habit habit = new Habit();
        habit.setUser(user);
        habit.setName(request.getName());
        habit.setDescription(request.getDescription());
        habit.setCategory(request.getCategory());
        habit.setFrequency(request.getFrequency() == null ? "DAILY" : request.getFrequency());
        habit.setTargetDaysPerWeek(
                request.getTargetDaysPerWeek() == null ? 7 : request.getTargetDaysPerWeek()
        );
        habit.setColor(request.getColor());
        habit.setIcon(request.getIcon());

        return toResponse(habitRepository.save(habit));
    }

    public HabitCompletionResponse complete(Long habitId, CompleteHabitRequest request) {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new ResourceNotFoundException("Hábito no encontrado."));

        LocalDate completedDate = request.getCompletedDate() == null
                ? LocalDate.now()
                : request.getCompletedDate();

        if (habitCompletionRepository.existsByHabitIdAndCompletedDate(habitId, completedDate)) {
            throw new IllegalStateException("Este hábito ya fue completado en esa fecha.");
        }

        HabitCompletion completion = new HabitCompletion();
        completion.setHabit(habit);
        completion.setCompletedDate(completedDate);
        completion.setNotes(request.getNotes());

        return HabitCompletionResponse.from(habitCompletionRepository.save(completion));
    }

    public List<HabitCompletionResponse> getCompletions(Long habitId) {
        return habitCompletionRepository.findByHabitIdOrderByCompletedDateDesc(habitId)
                .stream()
                .map(HabitCompletionResponse::from)
                .toList();
    }

    public void deactivate(Long habitId) {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new ResourceNotFoundException("Hábito no encontrado."));

        habit.setActive(false);
        habitRepository.save(habit);
    }

    private HabitResponse toResponse(Habit habit) {
        boolean completedToday = habitCompletionRepository
                .existsByHabitIdAndCompletedDate(habit.getId(), LocalDate.now());

        return HabitResponse.from(habit, completedToday);
    }
}