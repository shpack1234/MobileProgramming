/*
    ReceiptItemModel - MPTeamProject
    Copyright (C) 2024-2025 Coppermine-SP - <https://github.com/Coppermine-SP>.
 */
package com.ref.project.Models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReceiptItemModel {
    @JsonProperty("categoryId")
    public int CategoryId;

    @JsonProperty("itemDescription")
    public String ItemDescription;

    @JsonProperty("itemQuantity")
    public int ItemQuantity;
}
