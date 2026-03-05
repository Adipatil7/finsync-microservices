package com.settlementservice.domain.calculator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.expense.common.events.GroupExpenseCreatedEvent;
import com.settlementservice.entity.Settlement;

@Component
public class SettlementCalculator {
    
    public List<Settlement> calculateSettlements(GroupExpenseCreatedEvent event){

        List<Settlement> settlements = new ArrayList<>();

        UUID payer = event.getPaidBy();

        for(GroupExpenseCreatedEvent.ExpenseSplitPayload split : event.getSplits()){

            if(!split.getUserId().equals(payer)){

                Settlement settlement = new Settlement();
                settlement.setExpenseId(event.getExpenseId());
                settlement.setGroupId(event.getGroupId());
                settlement.setPaidBy(split.getUserId());
                settlement.setPaidTo(payer);
                settlement.setAmount(split.getAmountOwed());
                settlement.setCurrency(event.getCurrency());
                settlement.setSettlementDate(LocalDate.now());
                settlement.setCreatedAt(LocalDateTime.now());

                settlements.add(settlement);

            }

        }

        return settlements;

    }

}
