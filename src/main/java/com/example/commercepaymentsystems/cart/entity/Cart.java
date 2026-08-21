package com.example.commercepaymentsystems.cart.entity;



import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"customer_id"})

})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart extends com.example.commercepaymentsystems.common.entity.BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "customer_id", nullable = false)
    private Long customerId;


    public Cart(Long customerId){
        this.customerId = customerId;
    }
}
