package com.sgg.tenancy.entity;

public enum MemberRole {
    MEMBER,
    COACH,
    ADMIN,
    ADMIN_COACH  // Tiene permisos de ADMIN y COACH simultáneamente
}
