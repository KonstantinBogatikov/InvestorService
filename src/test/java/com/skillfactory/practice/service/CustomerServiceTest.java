package com.skillfactory.practice.service;

import com.skillfactory.practice.repository.CustomerRepository;
import com.skillfactory.practice.repository.OperationRepository;
import com.skillfactory.practice.entity.Customer;
import com.skillfactory.practice.entity.Operation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @InjectMocks
    private CustomerService service;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private OperationRepository operationRepository;

    @BeforeEach
    void setup() {}

    // Тестируем получение баланса клиента
    @Test
    void returnBalanceWhenCustomerExists() {
        long customerId = 1L;
        BigDecimal balance = new BigDecimal("100.00");
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(new Customer(balance)));

        Optional<BigDecimal> actualBalance = service.getBalance(customerId);

        assertEquals(Optional.of(balance), actualBalance);
    }

    // Тестируем, что возвращается Empty, если нет клиента
    @Test
    void returnEmptyIfCustomerDoesntExist() {
        long customerId = 1L;
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        Optional<BigDecimal> actualBalance = service.getBalance(customerId);

        assertTrue(actualBalance.isEmpty());
    }

    // Тестируем пополнение счета клиента
    @Test
    void depositMoneySuccessfully() {
        long customerId = 1L;
        BigDecimal currentBalance = new BigDecimal("100.00");
        BigDecimal depositAmount = new BigDecimal("50.00");
        BigDecimal expectedBalance = currentBalance.add(depositAmount);

        Customer customer = new Customer(currentBalance);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        service.putMoney(customerId, depositAmount);

        verify(customerRepository).save(customer);
        assertEquals(expectedBalance, customer.getBalance());
    }

    // Тестируем снятие средств со счета клиента
    @Test
    void withdrawMoneySuccessfully() {
        long customerId = 1L;
        BigDecimal currentBalance = new BigDecimal("100.00");
        BigDecimal withdrawAmount = new BigDecimal("50.00");
        BigDecimal expectedBalance = currentBalance.subtract(withdrawAmount);

        Customer customer = new Customer(currentBalance);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        boolean success = service.takeMoney(customerId, withdrawAmount);

        verify(customerRepository).save(customer);
        assertTrue(success);
        assertEquals(expectedBalance, customer.getBalance());
    }

    // Тестируем списание денег больше, чем есть на счете клиента
    @Test
    void failWithdrawIfInsufficientFunds() {
        long customerId = 1L;
        BigDecimal currentBalance = new BigDecimal("100.00");
        BigDecimal withdrawAmount = new BigDecimal("150.00");

        Customer customer = new Customer(currentBalance);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        boolean success = service.takeMoney(customerId, withdrawAmount);

        verify(customerRepository, never()).save(any());
        assertFalse(success);
    }

    // Тестируем перевод денег между клиентами
    @Test
    void transferMoneySuccessfully() {
        long senderId = 1L;
        long recipientId = 2L;
        BigDecimal transferAmount = new BigDecimal("50.00");

        Customer sender = new Customer(new BigDecimal("100.00"));
        Customer recipient = new Customer(new BigDecimal("80.00"));

        when(customerRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(customerRepository.findById(recipientId)).thenReturn(Optional.of(recipient));

        boolean success = service.transferMoney(senderId, recipientId, transferAmount);

        verify(customerRepository).save(sender);
        verify(customerRepository).save(recipient);
        assertTrue(success);
        assertEquals(new BigDecimal("50.00"), sender.getBalance());
        assertEquals(new BigDecimal("130.00"), recipient.getBalance());
    }

    // Тестируем попытку перевода средств больше, чем есть на счете клиента
    @Test
    void failTransferIfSenderHasNoEnoughFunds() {
        long senderId = 1L;
        long recipientId = 2L;
        BigDecimal transferAmount = new BigDecimal("150.00");

        Customer sender = new Customer(new BigDecimal("100.00"));
        Customer recipient = new Customer(new BigDecimal("80.00"));

        when(customerRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(customerRepository.findById(recipientId)).thenReturn(Optional.of(recipient));

        boolean success = service.transferMoney(senderId, recipientId, transferAmount);

        verify(customerRepository, never()).save(any());
        assertFalse(success);
    }

    // Тестируем получение списка операций за период
    @Test
    void shouldRetrieveOperationsWithinGivenPeriod() {
        long customerId = 1L;
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        List<Operation> operations = new ArrayList<>();
        when(operationRepository.findByCustomer_IdAndCreatedAtBetween(customerId, startDate, endDate))
                .thenReturn(operations);

        List<Operation> retrievedOperations = service.getOperationList(customerId, startDate, endDate);

        assertEquals(retrievedOperations, operations);
    }

    // Тестируем получение списка всех операций
    @Test
    void shouldRetrieveAllOperationsForCustomer() {
        long customerId = 1L;

        List<Operation> operations = new ArrayList<>();
        when(operationRepository.findAllByCustomer_Id(customerId)).thenReturn(operations);

        List<Operation> retrievedOperations = service.getOperationList(customerId, null, null);

        assertEquals(retrievedOperations, operations);
    }
}