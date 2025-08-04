package com.skillfactory.practice.config;

import com.skillfactory.practice.entity.Investor;
import com.skillfactory.practice.entity.Operation;
import com.skillfactory.practice.enums.OperationType;
import com.skillfactory.practice.repository.InvestorRepository;
import com.skillfactory.practice.repository.OperationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DataGenerator implements CommandLineRunner {

    private final InvestorRepository investorRepository;
    private final OperationRepository operationRepository;

    public DataGenerator(InvestorRepository investorRepository, OperationRepository operationRepository) {
        this.investorRepository = investorRepository;
        this.operationRepository = operationRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        generateAdditionalInvestorsIfNeeded();
        generateAdditionalTransactionsForNewInvestors();
    }

    private void generateAdditionalInvestorsIfNeeded() {
        long currentInvestorCount = investorRepository.count();
        int neededInvestors = Math.max(100 - (int)currentInvestorCount, 0);

        if (neededInvestors <= 0) {
            return;
        }

        Random random = new Random();
        List<Investor> additionalInvestors = new ArrayList<>();

        for (int i = 0; i < neededInvestors; i++) {
            Investor investor = new Investor();
            investor.setBalance(BigDecimal.valueOf(random.nextInt(10_000)));
            additionalInvestors.add(investor);
        }

        investorRepository.saveAll(additionalInvestors);
    }

    private void generateAdditionalTransactionsForNewInvestors() {
        long totalInvestors = investorRepository.count();
        long existingTransactionsCount = operationRepository.count();

        if (totalInvestors == 0 || existingTransactionsCount > 0) {
            return;
        }

        Random random = new Random();
        List<Investor> allInvestors = (List<Investor>) investorRepository.findAll();

        for (Investor investor : allInvestors) {
            int numberOfTransactions = random.nextInt(20) + 10; // От 10 до 29 транзакций
            List<Operation> transactions = new ArrayList<>();

            for (int j = 0; j < numberOfTransactions; j++) {
                Operation transaction = new Operation();
                transaction.setInvestor(investor);
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
