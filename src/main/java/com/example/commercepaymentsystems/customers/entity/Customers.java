package com.example.commercepaymentsystems.customers.entity;
import com.example.commercepaymentsystems.common.entity.BaseEntity;
import com.example.commercepaymentsystems.common.exception.BusinessException;
import com.example.commercepaymentsystems.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Customers extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String phoneNumber;

    @Column(nullable = false)
    private Long point = 0L;

    public Customers(String email, String password, String name, String phoneNumber) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public void updateInfo(String email, String name, String phoneNumber) {
        this.email = email;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }


    public void addPoint(long amount) {
        this.point += amount;
    }


    public void usePoint(Long pointUsed) {
        if (pointUsed > this.point) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_POINT);
        }
        this.point -= pointUsed;
    }

    public void revokePoint(Long pointUsed) {
        this.point += pointUsed;
    }

    public void restorePoint(long amount) {
        this.point += amount;
    }


}





