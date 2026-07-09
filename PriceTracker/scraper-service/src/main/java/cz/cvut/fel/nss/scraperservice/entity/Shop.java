package cz.cvut.fel.nss.scraperservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "shops")
@Data
public class Shop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "base_urls", nullable = false)
    private String baseUrls;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "reliability_score")
    private Integer reliabilityScore;

    @Column(name = "strategy_key", nullable = false)
    private String strategyKey;
}
