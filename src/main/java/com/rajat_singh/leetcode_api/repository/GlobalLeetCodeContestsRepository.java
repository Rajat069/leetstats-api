package com.rajat_singh.leetcode_api.repository;

import com.rajat_singh.leetcode_api.dto.ContestsDTO;
import com.rajat_singh.leetcode_api.entity.ContestDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;

@Repository
public interface GlobalLeetCodeContestsRepository extends JpaRepository<ContestDataEntity,Integer>, JpaSpecificationExecutor<ContestDataEntity> {

    ContestDataEntity findByTitleSlug(String titleSlug);
}
