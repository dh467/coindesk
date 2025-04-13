package com.example.repository;

import com.example.model.CurrencyMap;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyMapRepository extends JpaRepository<CurrencyMap, String> {
}