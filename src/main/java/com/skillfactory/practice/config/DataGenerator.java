package com.skillfactory.practice.config;

import com.skillfactory.practice.entity.Customer;
import com.skillfactory.practice.entity.Operation;
import com.skillfactory.practice.enums.OperationType;
import com.skillfactory.practice.repository.CustomerRepository;
import com.skillfactory.practice.repository.OperationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DataGenerator implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final OperationRepository operationRepository;

    public DataGenerator(CustomerRepository customerRepository, OperationRepository operationRepository) {
        this.customerRepository = customerRepository;
        this.operationRepository = operationRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        generateAdditionalCustomersIfNeeded();
        generateAdditionalTransactionsForNewCustomers();
    }

    private void generateAdditionalCustomersIfNeeded() {
        long currentCustomerCount = customerRepository.count();
        int neededCustomers = Math.max(100 - (int)currentCustomerCount, 0);

        if (neededCustomers <= 0) {
            return;
        }

        Random random = new Random();
        List<Customer> additionalCustomers = new ArrayList<>();

        for (int i = 0; i < neededCustomers; i++) {
            Customer customer = new Customer();
            customer.setBalance(BigDecimal.valueOf(random.nextInt(10_000)));
            additionalCustomers.add(customer);
        }

        customerRepository.saveAll(additionalCustomers);
    }

    private void generateAdditionalTransactionsForNewCustomers() {
        long totalCustomers = customerRepository.count();
        long existingTransactionsCount = operationRepository.count();

        if (totalCustomers == 0 || existingTransactionsCount > 0) {
            return;
        }

        Random random = new Random();
        List<Customer> allCustomers = (List<Customer>) customerRepository.findAll();

        for (Customer customer : allCustomers) {
            int numberOfTransactions = random.nextInt(20) + 10; // От 10 до 29 транзакций
            List<Operation> transactions = new ArrayList<>();

            for (int j = 0; j < numberOfTransactions; j++) {
                Operation transaction = new Operation();
                transaction.setCustomer(customer);
                transaction.setType(getRandomTransactionType());
                transaction.setAmount(BigDecimal.valueOf(random.nextInt(1000) + 1)); // Суммы от 1 до 1000
                transactions.add(transaction);
            }

            operationRepository.saveAll(transactions);
        }
    }

    private static OperationType getRandomTransactionType() {
        Random rand = new Random();
        return rand.nextBoolean() ? OperationType.DEPOSIT : OperationType.WITHDRAWAL;
    }
}
