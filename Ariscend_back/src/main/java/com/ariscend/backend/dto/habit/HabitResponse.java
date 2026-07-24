package com.ariscend.backend.dto.habit;

import com.ariscend.backend.entity.Habit;

public class HabitResponse {

    private Long id;
    private String name;
    private String description;
    private String category;
    private String frequency;
    private Integer targetDaysPerWeek;
    private String color;
    private String icon;
    private Boolean active;
    private Boolean completedToday;

    public static HabitResponse from(Habit habit, boolean completedToday) {
        HabitResponse response = new HabitResponse();

        response.id = habit.getId();
        response.name = habit.getName();
        response.description = habit.getDescription();
        response.category = habit.getCategory();
        response.frequency = habit.getFrequency();
        response.targetDaysPerWeek = habit.getTargetDaysPerWeek();
        response.color = habit.getColor();
        response.icon = habit.getIcon();
        response.active = habit.getActive();
        response.completedToday = completedToday;

        return response;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getFrequency() {
        return frequency;
    }

    public Integer getTargetDaysPerWeek() {
        return targetDaysPerWeek;
    }

    public String getColor() {
        return color;
    }

    public String getIcon() {
        return icon;
    }

    public Boolean getActive() {
        return active;
    }

    public Boolean getCompletedToday() {
        return completedToday;
    }
}