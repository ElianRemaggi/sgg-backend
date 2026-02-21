package com.sgg.common.security;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

/**
 * AOP Aspect que activa el Hibernate Filter "tenantFilter" automáticamente
 * en cada llamada a un repositorio, cuando hay un gymId en el contexto.
 *
 * Esto garantiza que todas las queries JPA queden filtradas por gym_id
 * sin necesidad de agregarlo manualmente en cada método de repositorio.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class TenantHibernateFilterAspect {

    private final EntityManager entityManager;

    @Before("execution(* com.sgg..repository.*Repository.*(..))")
    public void enableTenantFilter() {
        Long gymId = TenantContext.getGymId();
        if (gymId != null) {
            Session session = entityManager.unwrap(Session.class);
            session.enableFilter("tenantFilter")
                    .setParameter("gymId", gymId);
            log.trace("Hibernate tenantFilter enabled for gymId={}", gymId);
        }
    }
}
