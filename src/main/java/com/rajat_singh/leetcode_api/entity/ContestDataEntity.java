package com.rajat_singh.leetcode_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
@Table(name = "contest_data")
public class ContestDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String titleSlug;
    private long startTime;
    private long originStartTime;
    private String cardImg;

    @OneToMany(mappedBy = "contestData", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<SponsorEntity> sponsors;
}

