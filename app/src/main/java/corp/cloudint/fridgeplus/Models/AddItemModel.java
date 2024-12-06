/*
    AddItemModel - MPTeamProject
    Copyright (C) 2024-2025 Coppermine-SP - <https://github.com/Coppermine-SP>.
 */
package corp.cloudint.fridgeplus.Models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class AddItemModel {
    @JsonProperty("categoryId")
    public int CategoryId;

    @JsonProperty("itemDescription")
    public String ItemDescription;

    @JsonProperty("itemQuantity")
    public int ItemQuantity;

    @JsonProperty("expires")
    public LocalDate Expires;
}
