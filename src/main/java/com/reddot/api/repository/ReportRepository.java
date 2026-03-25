package com.reddot.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reddot.api.entity.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByResolvedAtIsNull();
}