package com.finai.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for manual category correction.
 * Conforms to Section 11: Manual Category Correction.
 */
public class CategoryUpdateRequestDTO {

    @NotBlank(message = "Category name cannot be blank")
    private String category;

    private String subcategory;

    public CategoryUpdateRequestDTO() {
    }

    public CategoryUpdateRequestDTO(String category, String subcategory) {
        this.category = category;
        this.subcategory = subcategory;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }
}
