package com.zenpay.domain.account.entity;

import com.zenpay.domain.user.entity.User;
import com.zenpay.global.common.BaseEntity;
import com.zenpay.global.exception.BusinessException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Account extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @Column(nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    public enum AccountStatus {
        ACTIVE, SUSPENDED, CLOSED
    }

    // 출금: 잔액 부족 or 계좌 정지 시 예외
    public void withdraw(BigDecimal amount) {
        if (this.status != AccountStatus.ACTIVE) {
            throw BusinessException.badRequest("정지되거나 폐쇄된 계좌입니다.");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw BusinessException.badRequest("잔액이 부족합니다.");
        }
        this.balance = this.balance.subtract(amount);
    }

    // 입금
    public void deposit(BigDecimal amount) {
        if (this.status != AccountStatus.ACTIVE) {
            throw BusinessException.badRequest("정지되거나 폐쇄된 계좌입니다.");
        }
        this.balance = this.balance.add(amount);
    }
}
