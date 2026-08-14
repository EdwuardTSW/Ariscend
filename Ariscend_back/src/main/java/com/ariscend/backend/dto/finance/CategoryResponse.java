package com.ariscend.backend.dto.finance;

import com.ariscend.backend.entity.CategoryType;
import com.ariscend.backend.entity.TransactionCategory;

public class CategoryResponse {
    private Long id;
    private String name;
    private CategoryType type;
    private boolean systemDefined;
    private String systemKey;
    private boolean active;

    public static CategoryResponse from(TransactionCategory category) {
        CategoryResponse response = new CategoryResponse();
        response.id = category.getId();
        response.name = category.getName();
        response.type = category.getType();
        response.systemDefined = category.isSystemDefined();
        response.systemKey = category.getSystemKey();
        response.active = category.isActive();
        return response;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public CategoryType getType() { return type; }
    public boolean isSystemDefined() { return systemDefined; }
    public String getSystemKey() { return systemKey; }
    public boolean isActive() { return active; }
}
