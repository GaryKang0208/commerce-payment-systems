package com.example.commercepaymentsystems.payments.entity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PaymentStatus {
    IN_PROGRESS {
        @Override
        public boolean canTransitTo(PaymentStatus target) {
            return target == PAID || target == FAILED;
        }
    },
    PAID {
        @Override
        public boolean canTransitTo(PaymentStatus target) {
            return target == CANCELLED;
        }
    },
    FAILED {
        @Override
        public boolean canTransitTo(PaymentStatus target) {
            return false;
        }
    },
    CANCELLED {
        @Override
        public boolean canTransitTo(PaymentStatus target) {
            return false;
        }
    };

    public abstract boolean canTransitTo(PaymentStatus target);
}
