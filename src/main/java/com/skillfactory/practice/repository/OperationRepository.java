package com.skillfactory.practice.repository;

import com.skillfactory.practice.entity.Operation;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OperationRepository extends CrudRepository<Operation, Long> {

    List<Operation> findAllByInvestor_Id(Long investorId);

    List<Operation> findByInvestor_IdAndCreatedAtBetween(Long investorId, LocalDateTime startDate, LocalDateTime endDate);

}
