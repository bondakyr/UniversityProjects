package cz.cvut.fel.nss.productcatalogservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "stock_alerts")
@Data
public class StockAlert {
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
    private StockConditionType conditionType;

    @Column(name = "threshold_value", nullable = false)
    private int thresholdValue;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
