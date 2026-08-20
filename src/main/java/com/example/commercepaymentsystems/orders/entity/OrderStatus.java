package com.example.commercepaymentsystems.orders.entity;

public enum OrderStatus {
    PENDING_PAYMENT {
        @Override
        public boolean canTransitTo(OrderStatus newStatus) {
            return newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.CANCELED;
        }
    },
    CONFIRMED {
        @Override
        public boolean canTransitTo(OrderStatus newStatus) {
            return newStatus == OrderStatus.CANCELED;
        }
    },
    CANCELED {
        @Override
        public boolean canTransitTo(OrderStatus newStatus) {
            return false;
        }
    };

    public abstract boolean canTransitTo(OrderStatus newStatus);
}
