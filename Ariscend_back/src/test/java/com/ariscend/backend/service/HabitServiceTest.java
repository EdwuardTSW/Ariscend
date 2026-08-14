package com.ariscend.backend.service;

import com.ariscend.backend.dto.completion.CompleteHabitRequest;
import com.ariscend.backend.dto.habit.CreateHabitRequest;
import com.ariscend.backend.dto.habit.HabitResponse;
import com.ariscend.backend.entity.AppUser;
import com.ariscend.backend.entity.Habit;
import com.ariscend.backend.exception.ResourceNotFoundException;
import com.ariscend.backend.repository.AppUserRepository;
import com.ariscend.backend.repository.HabitCompletionRepository;
import com.ariscend.backend.repository.HabitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HabitServiceTest {
    @Mock HabitRepository habitRepository;
    @Mock AppUserRepository appUserRepository;
    @Mock HabitCompletionRepository completionRepository;
    @InjectMocks HabitService service;

    @Test
    void createAssignsHabitToUserFromPath() {
        AppUser user = user(1L); CreateHabitRequest request = new CreateHabitRequest();
        request.setName("  Leer  ");
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(user));
        when(habitRepository.save(any(Habit.class))).thenAnswer(invocation -> {
            Habit habit = invocation.getArgument(0); habit.setId(5L); habit.assignCreatedAt(); return habit;
        });

        HabitResponse response = service.create(1L, request);

        assertEquals("Leer", response.getName());
        verify(habitRepository).save(any(Habit.class));
    }

    @Test
    void completeDoesNotAccessHabitOwnedByAnotherUser() {
        when(habitRepository.findByIdAndUserId(5L, 2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.complete(2L, 5L, new CompleteHabitRequest()));

        verify(completionRepository, never()).save(any());
    }

    @Test
    void completeRejectsInactiveHabit() {
        Habit habit = habit(1L); habit.setActive(false);
        when(habitRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(habit));

        assertThrows(IllegalStateException.class,
                () -> service.complete(1L, 5L, new CompleteHabitRequest()));

        verify(completionRepository, never()).save(any());
    }

    @Test
    void completeRejectsFutureDate() {
        Habit habit = habit(1L);
        CompleteHabitRequest request = new CompleteHabitRequest();
        request.setCompletedDate(LocalDate.now().plusDays(1));
        when(habitRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(habit));

        assertThrows(IllegalArgumentException.class, () -> service.complete(1L, 5L, request));

        verify(completionRepository, never()).save(any());
    }

    @Test
    void deactivateDoesNotModifyHabitOwnedByAnotherUser() {
        when(habitRepository.findByIdAndUserId(5L, 2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deactivate(2L, 5L));

        verify(habitRepository, never()).save(any());
    }

    private AppUser user(Long id) { AppUser user = new AppUser(); user.setId(id); return user; }
    private Habit habit(Long userId) {
        Habit habit = new Habit(); habit.setId(5L); habit.setUser(user(userId)); habit.setName("Leer"); return habit;
    }
}
