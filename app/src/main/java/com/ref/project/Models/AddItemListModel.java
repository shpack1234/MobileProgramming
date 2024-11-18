/*
    AddItemListModel - MPTeamProject
    Copyright (C) 2024-2025 Coppermine-SP - <https://github.com/Coppermine-SP>.
 */
package com.ref.project.Models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AddItemListModel {
    @JsonProperty("items")
    public List<AddItemModel> Items;
}
