package com.personalservice.mapper;

import com.personalservice.dto.BudgetRequest;
import com.personalservice.dto.BudgetResponse;
import com.personalservice.entity.Budget;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BudgetMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Budget toEntity(BudgetRequest request);

    @Mapping(target = "categoryName", ignore = true)
    @Mapping(target = "spentAmount", ignore = true)
    @Mapping(target = "remainingAmount", ignore = true)
    BudgetResponse toResponse(Budget budget);
}
