package eu.mizerak.alemiz.translationlib.service.access;

import io.javalin.security.RouteRole;

public enum AccessRole implements RouteRole {
    PUBLIC,
    PRIVATE
}
