package com.skillfactory.practice.service;

import com.skillfactory.practice.entity.Investor;
import com.skillfactory.practice.entity.Operation;
import com.skillfactory.practice.enums.OperationType;
import com.skillfactory.practice.repository.InvestorRepository;
import com.skillfactory.practice.repository.OperationRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InvestorService {

    private final InvestorRepository investorRepository;
    private final OperationRepository operationRepository;

    public InvestorService(InvestorRepository investorRepository, OperationRepository operationRepository) {
        this.investorRepository = investorRepository;
        this.operationRepository = operationRepository;
    }

    // Получение текущего баланса пользователя
    public Optional<BigDecimal> getBalance(Long investorId) {
        return investorRepository.findById(investorId).map(Investor::getBalance);
    }

    // Пополнение счета пользователя
    @Transactional
    public void putMoney(Long investorId, BigDecimal amount) {
        Optional<Investor> investorOptional = investorRepository.findById(investorId);
        if (investorOptional.isPresent()) {
            Investor investor = investorOptional.get();
            investor.setBalance(investor.getBalance().add(amount));
            investorRepository.save(investor);

            saveOperation(investorId, OperationType.DEPOSIT, amount);
        }
    }

    // Снятие денег со счёта пользователя
    @Transactional
    public boolean takeMoney(Long investorId, BigDecimal amount) {
        Optional<Investor> investorOptional = investorRepository.findById(investorId);
        if (investorOptional.isPresent() && investorOptional.get().getBalance().compareTo(amount) >= 0) {
            Investor investor = investorOptional.get();
            investor.setBalance(investor.getBalance().subtract(amount));
            investorRepository.save(investor);

            saveOperation(investorId,OperationType.WITHDRAWAL, amount);

            return true;
        }
        return false;
    }

    private void saveOperation(long investorId, OperationType type, BigDecimal amount) {
        Optional<Investor> investorOptional = investorRepository.findById(investorId);
        if (investorOptional.isPresent()) {
            Investor investor = investorOptional.get();
            Operation operation = new Operation();
            operation.setInvestor(investor);
            operation.setType(type);
            operation.setAmount(amount);
            operationRepository.save(operation);
        } else {
            throw new IllegalArgumentException("Инвестор с данным ID не найден.");
        }
    }

    public List<Operation> getOperationList(Long investorId, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null || endDate != null) {
            return operationRepository.findByInvestor_IdAndCreatedAtBetween(investorId, startDate, endDate);
        } else {
            return operationRepository.findAllByInvestor_Id(investorId);
        }
    }

}
