package com.skillfactory.practice.config;

import com.skillfactory.practice.entity.Customer;
import com.skillfactory.practice.entity.Operation;
import com.skillfactory.practice.enums.OperationType;
import com.skillfactory.practice.repository.CustomerRepository;
import com.skillfactory.practice.repository.OperationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        generateTransferOperationsBetweenCustomers();
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

    private void generateTransferOperationsBetweenCustomers() {
        List<Customer> allCustomers = (List<Customer>) customerRepository.findAll();
        if (allCustomers.size() < 2) {
            return;
        }

        Random random = new Random();

        // Количество переводов для генерации
        int numberOfTransfers = 50;

        List<Operation> transfers = new ArrayList<>();

        for (int i = 0; i < numberOfTransfers; i++) {
            // Выбираем двух случайных разных клиентов
            Customer sender = allCustomers.get(random.nextInt(allCustomers.size()));
            Customer recipient;
            do {
                recipient = allCustomers.get(random.nextInt(allCustomers.size()));
            } while (recipient.getId() == sender.getId());

            // Генерируем сумму перевода (примерно половина баланса отправителя)
            BigDecimal amount = sender.getBalance()
                    .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);

            // Создаем операцию списания у отправителя
            Operation withdrawal = new Operation();
            withdrawal.setCustomer(sender);
            withdrawal.setType(OperationType.WITHDRAWAL);
            withdrawal.setAmount(amount);
            transfers.add(withdrawal);

            // Создаем операцию зачисления у получателя
            Operation deposit = new Operation();
            deposit.setCustomer(recipient);
            deposit.setType(OperationType.DEPOSIT);
            deposit.setAmount(amount);
            transfers.add(deposit);

            // Обновляем балансы клиентов
            sender.setBalance(sender.getBalance().subtract(amount));
            recipient.setBalance(recipient.getBalance().add(amount));
        }

        // Сохраняем все операции и обновленных клиентов
        operationRepository.saveAll(transfers);
        customerRepository.saveAll(allCustomers);
    }

    private static OperationType getRandomTransactionType() {
        Random rand = new Random();
        return rand.nextBoolean() ? OperationType.DEPOSIT : OperationType.WITHDRAWAL;
    }
}
