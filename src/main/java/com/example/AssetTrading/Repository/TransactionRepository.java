package com.example.AssetTrading.Repository;

import com.example.AssetTrading.Entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByBuyer(User buyer);

    List<Transaction> findBySeller(User seller);

    List<Transaction> findByStatus(TransactionStatus status);

}
