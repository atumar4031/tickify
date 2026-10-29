package com.aasismgt.reponse;

import com.aasismgt.entities.User;
import com.aasismgt.enums.GeneralStatus;
import com.aasismgt.enums.Roles;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Roles role;
    private GeneralStatus status;

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getFirstName(), user.getLastName(),
                user.getEmail(), user.getRole(), user.getStatus());
    }
}
