package ra.edu.hw.md04.ss14.repository;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ra.edu.hw.md04.ss14.entity.RefreshToken;
import ra.edu.hw.md04.ss14.entity.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("delete from RefreshToken refreshToken where refreshToken.token = :token and refreshToken.expiryDate > :now")
    int consumeValidToken(@Param("token") String token, @Param("now") Instant now);

    void deleteByUser(User user);
}
