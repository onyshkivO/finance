package com.onyshkiv.finance.repository;

import com.onyshkiv.finance.model.entity.MonobankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MonobankAccountRepository extends JpaRepository<MonobankAccount, UUID> {
    @Modifying
    @Query(value = """
                INSERT INTO monobank_account (id, user_id, client_id, name, account_id, send_id, iban, currency_code, type, created_at, updated_at)
                VALUES (:#{#account.id}, :#{#account.userId}, :#{#account.clientId}, :#{#account.name}, :#{#account.accountId},\s
                        :#{#account.sendId}, :#{#account.iban}, :#{#account.currencyCode}, CAST(:#{#account.type.name()} AS monobank_account_type_enum),\s
                        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                ON CONFLICT ON CONSTRAINT uk_monobank_account_user_account
                DO UPDATE SET\s
                    client_id = EXCLUDED.client_id,
                    name = EXCLUDED.name,
                    send_id = EXCLUDED.send_id,
                    iban = EXCLUDED.iban,
                    currency_code = EXCLUDED.currency_code,
                    type = EXCLUDED.type,
                    updated_at = CURRENT_TIMESTAMP
           \s""", nativeQuery = true)
    void upsertMonobankAccount(@Param("account") MonobankAccount account);

    Optional<MonobankAccount> findByAccountId(String accountId);
}
