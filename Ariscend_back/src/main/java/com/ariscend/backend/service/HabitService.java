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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
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
        if (!appUserRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario no encontrado.");
        }
        Set<Long> completedToday = new HashSet<>(habitCompletionRepository.findCompletedHabitIds(
                userId, LocalDate.now()
        ));
        return habitRepository.findByUserIdAndActiveTrueOrderByCreatedAtDesc(userId)
                .stream()
                .map(habit -> HabitResponse.from(habit, completedToday.contains(habit.getId())))
                .toList();
    }

    @Transactional
    public HabitResponse create(Long userId, CreateHabitRequest request) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        Habit habit = new Habit();
        habit.setUser(user);
        habit.setName(request.getName().trim());
        habit.setDescription(request.getDescription());
        habit.setCategory(request.getCategory());
        habit.setFrequency(request.getFrequency() == null ? "DAILY" : request.getFrequency());
        habit.setTargetDaysPerWeek(
                request.getTargetDaysPerWeek() == null ? 7 : request.getTargetDaysPerWeek()
        );
        habit.setColor(request.getColor());
        habit.setIcon(request.getIcon());

        return HabitResponse.from(habitRepository.save(habit), false);
    }

    @Transactional
    public HabitCompletionResponse complete(Long userId, Long habitId, CompleteHabitRequest request) {
        Habit habit = findOwnedHabit(userId, habitId);
        if (!habit.getActive()) {
            throw new IllegalStateException("No se puede completar un hábito inactivo.");
        }

        LocalDate completedDate = request.getCompletedDate() == null
                ? LocalDate.now()
                : request.getCompletedDate();
        if (completedDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de finalización no puede ser futura.");
        }

        if (habitCompletionRepository.existsByHabitIdAndCompletedDate(habitId, completedDate)) {
            throw new IllegalStateException("Este hábito ya fue completado en esa fecha.");
        }

        HabitCompletion completion = new HabitCompletion();
        completion.setHabit(habit);
        completion.setCompletedDate(completedDate);
        completion.setNotes(request.getNotes());

        return HabitCompletionResponse.from(habitCompletionRepository.save(completion));
    }

    public List<HabitCompletionResponse> getCompletions(Long userId, Long habitId) {
        findOwnedHabit(userId, habitId);
        return habitCompletionRepository.findByHabitIdOrderByCompletedDateDesc(habitId)
                .stream()
                .map(HabitCompletionResponse::from)
                .toList();
    }

    public List<HabitCompletionResponse> getAllCompletions(Long userId) {
        if (!appUserRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario no encontrado.");
        }
        return habitCompletionRepository.findActiveHabitCompletionsByUserId(userId).stream()
                .map(HabitCompletionResponse::from)
                .toList();
    }

    @Transactional
    public void deactivate(Long userId, Long habitId) {
        Habit habit = findOwnedHabit(userId, habitId);

        habit.setActive(false);
        habitRepository.save(habit);
    }

    private Habit findOwnedHabit(Long userId, Long habitId) {
        return habitRepository.findByIdAndUserId(habitId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Hábito no encontrado."));
    }
}
