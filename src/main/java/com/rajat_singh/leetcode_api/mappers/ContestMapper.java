package com.rajat_singh.leetcode_api.mappers;

import com.rajat_singh.leetcode_api.dto.ContestDTO;
import com.rajat_singh.leetcode_api.dto.ContestsDTO;
import com.rajat_singh.leetcode_api.entity.ContestDataEntity;
import com.rajat_singh.leetcode_api.entity.SponsorEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ContestMapper {
    ContestDTO entityToDTO(ContestDataEntity entity);
    SponsorEntity dtoToSponsorEntity(ContestsDTO.Sponsor dto);
    ContestDataEntity dtoToEntity(ContestDTO dto);
}
