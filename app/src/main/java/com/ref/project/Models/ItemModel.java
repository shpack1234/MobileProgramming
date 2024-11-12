/*
    ItemModel - MPTeamProject
    Copyright (C) 2024-2025 Coppermine-SP - <https://github.com/Coppermine-SP>.
 */
package com.ref.project.Models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class ItemModel {
    @JsonProperty("itemId")
    public int ItemId;

    @JsonProperty("categoryId")
    public int CategoryId;

    @JsonProperty("itemOwner")
    public String ItemOwner;

    @JsonProperty("itemDescription")
    public String ItemDescription;

    @JsonProperty("itemQuantity")
    public int ItemQuantity;

    @JsonProperty("itemImportDate")
    public Date ItemImportDate;

    @JsonProperty("itemExpireDate")
    public Date ItemExpireDate;
}
