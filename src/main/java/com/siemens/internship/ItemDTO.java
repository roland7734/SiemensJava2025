package com.siemens.internship;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO used for creating or updating Item entities via API.
 * Includes validation rules to ensure client input is correct.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemDTO {

    // Name must not be empty
    @NotBlank(message = "Name is required")
    private String name;

    // Optional description
    private String description;

    // Must be a valid email format if provided
    @Email(message = "Email should be valid")
    private String email;

    // Item status (e.g., NEW, PROCESSED)
    private String status;
}
