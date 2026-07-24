package com.ariscend.backend.dto.habit;

public class CreateHabitRequest {

    private Long userId;
    private String name;
    private String description;
    private String category;
    private String frequency;
    private Integer targetDaysPerWeek;
    private String color;
    private String icon;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public Integer getTargetDaysPerWeek() {
        return targetDaysPerWeek;
    }

    public void setTargetDaysPerWeek(Integer targetDaysPerWeek) {
        this.targetDaysPerWeek = targetDaysPerWeek;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}