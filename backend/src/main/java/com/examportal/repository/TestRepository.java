package com.examportal.repository;

import com.examportal.model.Test;
import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface TestRepository extends JpaRepository<Test, Long> {
	@EntityGraph(attributePaths = "questions")
	Optional<Test> findWithQuestionsById(Long id);

	List<Test> findByPublishedTrue();
}
