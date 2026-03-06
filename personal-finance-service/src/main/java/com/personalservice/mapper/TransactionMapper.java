package com.personalservice.mapper;

import com.personalservice.dto.TransactionRequest;
import com.personalservice.dto.TransactionResponse;
import com.personalservice.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Transaction toEntity(TransactionRequest request);

    @Mapping(target = "categoryName", ignore = true)
    TransactionResponse toResponse(Transaction transaction);
}
