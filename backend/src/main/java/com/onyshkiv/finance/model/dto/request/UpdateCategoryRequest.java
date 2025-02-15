package com.onyshkiv.finance.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UpdateCategoryRequest {
    @NotBlank(message = "invalid category name")
    private String name;

    private Set<Integer> mccCodes;
}
