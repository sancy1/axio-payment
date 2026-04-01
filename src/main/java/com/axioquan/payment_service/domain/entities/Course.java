// Path: src/main/java/com/axioquan/payment_service/domain/entities/Course.java

package com.axioquan.payment_service.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    private UUID id;

    private String title;

    @Column(name = "price_cents")
    private Integer priceCents;

    private String currency;

    @Column(name = "is_published")
    private Boolean isPublished;
}