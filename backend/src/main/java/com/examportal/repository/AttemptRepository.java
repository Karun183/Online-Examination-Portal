package com.examportal.repository;

import com.examportal.model.*;
import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {
	Optional<Attempt> findByUserIdAndTestId(Long userId, Long testId);

	List<Attempt> findByUserIdOrderByStartedAtDesc(Long userId);

	@Query("select a from Attempt a join fetch a.user join fetch a.test order by a.startedAt desc")
	List<Attempt> findAllDetailed();
}
