package com.ariscend.backend.dto.habit;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateHabitRequest {

    @NotBlank(message = "El nombre del hábito es obligatorio.")
    @Size(max = 120, message = "El nombre no puede superar los 120 caracteres.")
    private String name;

    @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres.")
    private String description;

    @Size(max = 80, message = "La categoría no puede superar los 80 caracteres.")
    private String category;

    @Pattern(regexp = "DAILY|WEEKLY", message = "La frecuencia debe ser DAILY o WEEKLY.")
    private String frequency;

    @Min(value = 1, message = "Los días objetivo deben ser al menos 1.")
    @Max(value = 7, message = "Los días objetivo no pueden superar 7.")
    private Integer targetDaysPerWeek;

    @Size(max = 20, message = "El color no puede superar los 20 caracteres.")
    private String color;

    @Size(max = 50, message = "El icono no puede superar los 50 caracteres.")
    private String icon;

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
