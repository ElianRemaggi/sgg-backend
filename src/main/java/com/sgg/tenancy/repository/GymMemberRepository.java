package com.sgg.tenancy.repository;

import com.sgg.tenancy.entity.GymMember;
import com.sgg.tenancy.entity.MemberRole;
import com.sgg.tenancy.entity.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GymMemberRepository extends JpaRepository<GymMember, Long> {

    Optional<GymMember> findByUserIdAndGymIdAndStatusIn(
            Long userId, Long gymId, List<MembershipStatus> statuses);

    List<GymMember> findByUserIdAndStatusIn(Long userId, List<MembershipStatus> statuses);

    List<GymMember> findByGymId(Long gymId);

    boolean existsByUserIdAndGymIdAndStatus(Long userId, Long gymId, MembershipStatus status);

    List<GymMember> findByGymIdAndRoleInAndStatus(Long gymId, List<MemberRole> roles, MembershipStatus status);

    long countByGymIdAndStatus(Long gymId, MembershipStatus status);

    long countByGymIdAndRoleInAndStatus(Long gymId, List<MemberRole> roles, MembershipStatus status);

    /**
     * Query sin filtro Hibernate — necesaria para el TenantInterceptor
     * que valida la membresía ANTES de setear el TenantContext.
     */
    @Query("SELECT gm FROM GymMember gm WHERE gm.userId = :userId AND gm.gymId = :gymId AND gm.status IN :statuses")
    Optional<GymMember> findMembershipDirect(
            @Param("userId") Long userId,
            @Param("gymId") Long gymId,
            @Param("statuses") List<MembershipStatus> statuses);
}
