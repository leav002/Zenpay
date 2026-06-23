package com.zenpay.domain.transaction.repository;

import com.zenpay.domain.transaction.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("""
        SELECT t FROM Transaction t
        WHERE t.senderAccount.id = :accountId
           OR t.receiverAccount.id = :accountId
        ORDER BY t.createdAt DESC
    """)
    Page<Transaction> findAllByAccountId(Long accountId, Pageable pageable);
}
