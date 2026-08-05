package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.repositories;



import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.modelDB.HEADAuthRefreshToken;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface HEADAuthRefreshTokenRepository extends JpaRepository<HEADAuthRefreshToken, Long> {

    @Query("""
      select t from HEADAuthRefreshToken t
       where t.userId = :userId and t.deviceId = :deviceId and t.revoked = false
       order by t.issuedAt desc
    """)
    List<HEADAuthRefreshToken> findActiveByUserAndDevice(@Param("userId") Long userId,
                                                         @Param("deviceId") String deviceId);

    @Query("""
      select t from HEADAuthRefreshToken t
       where t.userId = :userId and t.deviceId = :deviceId and t.tokenHash = :hash
    """)
    Optional<HEADAuthRefreshToken> findOne(@Param("userId") Long userId,
                                           @Param("deviceId") String deviceId,
                                           @Param("hash") String hash);

    @Modifying @Query("""
      update HEADAuthRefreshToken t
         set t.revoked = true
       where t.userId = :userId and t.deviceId = :deviceId and t.revoked = false
    """)
    int revokeAllByDevice(@Param("userId") Long userId, @Param("deviceId") String deviceId);

    Optional<HEADAuthRefreshToken> findByTokenHash(String token);

    @Modifying
    @Transactional
    @Query("""
  update HEADAuthRefreshToken t
     set t.revoked = true
   where t.userId = :userId
     and t.revoked = false
""")
    int revokeAllByUserId(@Param("userId") Long userId);
}
