/*
    CategoryModel - MPTeamProject
    Copyright (C) 2024-2025 Coppermine-SP - <https://github.com/Coppermine-SP>.
 */
package corp.cloudint.fridgeplus.Models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CategoryModel {
    @JsonProperty("categoryId")
    public int CategoryId;

    @JsonProperty("categoryName")
    public String CategoryName;

    @JsonProperty("recommendedExpirationDays")
    public Integer RecommendedExpirationDays;
}
