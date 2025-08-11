package com.skillfactory.practice.repository;

import com.skillfactory.practice.entity.Operation;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OperationRepository extends CrudRepository<Operation, Long> {

    List<Operation> findAllByCustomer_Id(Long customerId);

    List<Operation> findByCustomer_IdAndCreatedAtBetween(Long customerId, LocalDateTime startDate, LocalDateTime endDate);

}
