package ra.edu.hw.md04.ss14.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ra.edu.hw.md04.ss14.entity.RefreshToken;
import ra.edu.hw.md04.ss14.entity.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);
}
