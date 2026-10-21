package com.example.commercepaymentsystems.point.service;
import com.example.commercepaymentsystems.common.exception.BusinessException;
import com.example.commercepaymentsystems.common.exception.ErrorCode;
import com.example.commercepaymentsystems.customers.entity.Customers;
import com.example.commercepaymentsystems.customers.repository.CustomersRepository;
import com.example.commercepaymentsystems.payments.entity.Payment;
import com.example.commercepaymentsystems.point.dto.PointBalanceResponse;
import com.example.commercepaymentsystems.point.dto.PointTransactionResponse;
import com.example.commercepaymentsystems.point.entity.Point;
import com.example.commercepaymentsystems.point.entity.PointTransactionType;
import com.example.commercepaymentsystems.point.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PointService {
    private final CustomersRepository customersRepository;
    private final PointRepository pointRepository;

    @Transactional(readOnly = true)
    public PointBalanceResponse getBalance(Long customerId) {
        Customers customer = customersRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
        return new PointBalanceResponse(customer.getPoint());
    }

    @Transactional(readOnly = true)
    public List<PointTransactionResponse> getHistory(Long customerId) {
        return pointRepository.findByCustomersIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(PointTransactionResponse::from)
                .toList();
    }

    private Customers getLockedCustomer(Long customerId) {
        return customersRepository.findByIdForUpdate(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
    }

    public void earn(Customers customer, Payment payment, Long amount) {
        Customers lockedCustomer = getLockedCustomer(customer.getId());
        lockedCustomer.addPoint(amount);
        pointRepository.save(
                new Point(lockedCustomer, payment, PointTransactionType.EARN, amount)
        );
    }

    public void use(Customers customer, Payment payment, Long amount) {
        Customers lockedCustomer = getLockedCustomer(customer.getId());
        lockedCustomer.usePoint(amount);
        pointRepository.save(
                new Point(lockedCustomer, payment, PointTransactionType.USE, -amount)
        );
    }

    public void revokeEarn(Customers customer, Payment payment, Long amount) {
        Customers lockedCustomer = getLockedCustomer(customer.getId());
        lockedCustomer.revokePoint(amount);
        pointRepository.save(
                new Point(lockedCustomer, payment, PointTransactionType.REVOKEEARN, -amount)
        );
    }

    public void restoreUse(Customers customer, Payment payment, Long amount) {
        Customers lockedCustomer = getLockedCustomer(customer.getId());
        lockedCustomer.restorePoint(amount);
        pointRepository.save(
                new Point(lockedCustomer, payment, PointTransactionType.RESTOREUSE, amount)
        );
    }
}



