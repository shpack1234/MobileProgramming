/*
    RecipeModel - MPTeamProject
    Copyright (C) 2024-2025 Coppermine-SP - <https://github.com/Coppermine-SP>.
 */
package com.ref.project.Models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public class RecipeModel implements Serializable {
    public static final String RECIPE_MODEL_KEY = "RecipeModel";

    @JsonProperty("name")
    public String Name;

    @JsonProperty("description")
    public String Description;

    @JsonProperty("steps")
    public List<String> Steps;
}
