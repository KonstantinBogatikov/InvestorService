package com.skillfactory.practice.service;

import com.skillfactory.practice.entity.Customer;
import com.skillfactory.practice.entity.Operation;
import com.skillfactory.practice.enums.OperationType;
import com.skillfactory.practice.repository.CustomerRepository;
import com.skillfactory.practice.repository.OperationRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final OperationRepository operationRepository;

    public CustomerService(CustomerRepository customerRepository, OperationRepository operationRepository) {
        this.customerRepository = customerRepository;
        this.operationRepository = operationRepository;
    }

    // Получение текущего баланса пользователя
    public Optional<BigDecimal> getBalance(Long customerId) {
        return customerRepository.findById(customerId).map(Customer::getBalance);
    }

    // Пополнение счета пользователя
    @Transactional
    public void putMoney(Long customerId, BigDecimal amount) {
        Optional<Customer> customerOptional = customerRepository.findById(customerId);
        if (customerOptional.isPresent()) {
            Customer customer = customerOptional.get();
            customer.setBalance(customer.getBalance().add(amount));
            System.out.println(customer.getBalance());
            customerRepository.save(customer);

            saveOperation(customerId, OperationType.DEPOSIT, amount);
        }
    }

    // Снятие денег со счёта пользователя
    @Transactional
    public boolean takeMoney(Long customerId, BigDecimal amount) {
        Optional<Customer> customerOptional = customerRepository.findById(customerId);
        if (customerOptional.isPresent() && customerOptional.get().getBalance().compareTo(amount) >= 0) {
            Customer customer = customerOptional.get();
            customer.setBalance(customer.getBalance().subtract(amount));
            customerRepository.save(customer);

            saveOperation(customerId,OperationType.WITHDRAWAL, amount);

            return true;
        }
        return false;
    }

    @Transactional
    public boolean transferMoney(Long senderId, Long recipientId, BigDecimal amount) {
        Optional<Customer> senderOptional = customerRepository.findById(senderId);
        Optional<Customer> recipientOptional = customerRepository.findById(recipientId);

        // Проверка, что клиенты существуют
        if (senderOptional.isEmpty() || recipientOptional.isEmpty()) {
            return false;
        }

        Customer sender = senderOptional.get();
        Customer recipient = recipientOptional.get();

        // Проверка, что достаточно средств
        if (sender.getBalance().compareTo(amount) < 0) {
            return false;
        }

        // Переводим деньги между клиентами
        synchronized (this) {
            sender.setBalance(sender.getBalance().subtract(amount));
            recipient.setBalance(recipient.getBalance().add(amount));

            customerRepository.save(sender);
            customerRepository.save(recipient);

            // Записываем в БД информацию о транзакциях
            saveOperation(senderId, OperationType.WITHDRAWAL, amount);
            saveOperation(recipientId, OperationType.DEPOSIT, amount);
        }

        return true;
    }

    private void saveOperation(long customerId, OperationType type, BigDecimal amount) {
        Optional<Customer> customerOptional = customerRepository.findById(customerId);
        if (customerOptional.isPresent()) {
            Customer customer = customerOptional.get();
            Operation operation = new Operation();
            operation.setCustomer(customer);
            operation.setType(type);
            operation.setAmount(amount);
            operationRepository.save(operation);
        } else {
            throw new IllegalArgumentException("Инвестор с данным ID не найден.");
        }
    }

    public List<Operation> getOperationList(Long customerId, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null || endDate != null) {
            return operationRepository.findByCustomer_IdAndCreatedAtBetween(customerId, startDate, endDate);
        } else {
            return operationRepository.findAllByCustomer_Id(customerId);
        }
    }

}
