package com.siemens.internship;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemDTO {
    @NotBlank
    private String name;

    private String description;

    @Email(message = "Email should be valid")
    private String email;

    private String status;
}
