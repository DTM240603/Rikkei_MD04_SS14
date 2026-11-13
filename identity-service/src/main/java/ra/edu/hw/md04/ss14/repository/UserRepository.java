package ra.edu.hw.md04.ss14.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ra.edu.hw.md04.ss14.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}
