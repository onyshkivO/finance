package com.onyshkiv.finance.repository;

import com.onyshkiv.finance.model.entity.MonobankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MonobankAccountRepository extends JpaRepository<MonobankAccount, UUID> {

}
