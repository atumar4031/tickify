package com.aasismgt.request;

import javax.management.relation.Role;

public record UserRequest(String firstName, String lastName, String email, String role, String password) { }
