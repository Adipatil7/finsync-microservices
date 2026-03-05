package com.settlementservice.service.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.expense.common.events.GroupExpenseCreatedEvent;
import com.settlementservice.domain.calculator.SettlementCalculator;
import com.settlementservice.entity.Settlement;
import com.settlementservice.repository.SettlementRepository;
import com.settlementservice.service.SettlementService;

@Service
public class SettlementServiceImpl implements SettlementService {
    
    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private SettlementCalculator settlementCalculator;

    @Override
    public void processExpenseEvent(GroupExpenseCreatedEvent event) {

        List<Settlement> settlements = this.settlementCalculator.calculateSettlements(event);
        this.settlementRepository.saveAll(settlements);
    }

}
