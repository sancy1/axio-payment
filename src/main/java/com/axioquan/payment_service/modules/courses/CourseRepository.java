
// Path: src/main/java/com/axioquan/payment_service/modules/courses/CourseRepository.java


package com.axioquan.payment_service.modules.courses;

import com.axioquan.payment_service.domain.entities.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {
}