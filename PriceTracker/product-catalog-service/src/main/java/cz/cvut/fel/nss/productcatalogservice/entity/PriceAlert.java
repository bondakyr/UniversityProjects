package cz.cvut.fel.nss.productcatalogservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "price_alerts")
@Data
public class PriceAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false)
    private PriceConditionType conditionType;

    @Column(name = "threshold_value", precision = 12, scale = 2, nullable = false)
    private BigDecimal thresholdValue;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
