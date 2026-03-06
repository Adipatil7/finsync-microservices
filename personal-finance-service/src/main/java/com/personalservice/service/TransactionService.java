package com.personalservice.service;

import com.personalservice.dto.TransactionRequest;
import com.personalservice.dto.TransactionResponse;
import com.personalservice.entity.Category;
import com.personalservice.entity.Transaction;
import com.personalservice.exception.ResourceNotFoundException;
import com.personalservice.exception.ValidationException;
import com.personalservice.mapper.TransactionMapper;
import com.personalservice.repository.CategoryRepository;
import com.personalservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;

    @Transactional
    public TransactionResponse createTransaction(UUID userId, TransactionRequest request) {
        Category category = categoryRepository.findByIdAndUserId(request.getCategoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (category.getType() != request.getType()) {
            throw new ValidationException("Transaction type must match Category type");
        }

        Transaction transaction = transactionMapper.toEntity(request);
        transaction.setUserId(userId);

        transaction = transactionRepository.save(transaction);
        
        TransactionResponse response = transactionMapper.toResponse(transaction);
        response.setCategoryName(category.getName());
        return response;
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactions(UUID userId, LocalDate from, LocalDate to) {
        List<Transaction> transactions;
        
        if (from != null && to != null) {
            transactions = transactionRepository.findByUserIdAndTransactionDateBetween(userId, from, to);
        } else {
            transactions = transactionRepository.findByUserId(userId);
        }

        return transactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteTransaction(UUID userId, UUID id) {
        Transaction transaction = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
                
        transactionRepository.delete(transaction);
    }
    
    private TransactionResponse mapToResponse(Transaction t) {
        TransactionResponse response = transactionMapper.toResponse(t);
        categoryRepository.findById(t.getCategoryId())
                .ifPresent(c -> response.setCategoryName(c.getName()));
        return response;
    }
}
