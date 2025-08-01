package com.skillfactory.practice.repository;

import com.skillfactory.practice.entity.Investor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvestorRepository extends CrudRepository<Investor, Long> {
}
