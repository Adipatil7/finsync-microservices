package com.groupservice.split;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.groupservice.enums.SplitType;

@Component
public class SplitStrategyFactory {
    
    @Autowired
    private EqualSplitStrategy equalSplitStrategy;

    @Autowired
    private ExactSplitStrategy exactSplitStrategy;

    @Autowired
    private PercentageSplitStrategy percentageSplitStrategy;

    public SplitStrategy getStrategy(SplitType type){
        switch(type){
            case EQUAL:
                return equalSplitStrategy;
            case EXACT:
                return exactSplitStrategy;
            case PERCENTAGE:
                return percentageSplitStrategy;
            default:
                throw new IllegalArgumentException("Invalid split type: " + type);
        }
    }

}
