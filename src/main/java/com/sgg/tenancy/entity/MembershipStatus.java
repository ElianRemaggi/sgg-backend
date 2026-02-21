package com.sgg.tenancy.entity;

public enum MembershipStatus {
    PENDING,    // Solicitud enviada, esperando aprobación
    ACTIVE,     // Aprobada y vigente
    REJECTED,   // Rechazada por el administrador
    BLOCKED     // Bloqueada manualmente por el administrador
}
