package com.groupservice.service.serviceImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.expense.common.events.GroupExpenseCreatedEvent;
import com.expense.common.events.GroupExpenseCreatedEvent.ExpenseSplitPayload;
import com.groupservice.dto.AddMemberRequest;
import com.groupservice.dto.CreateExpenseRequest;
import com.groupservice.dto.CreateGroupRequest;
import com.groupservice.dto.ExpenseSplitRequest;
import com.groupservice.entity.Group;
import com.groupservice.entity.GroupExpense;
import com.groupservice.entity.GroupExpenseSplit;
import com.groupservice.entity.GroupMember;
import com.groupservice.kafka.GroupEventProducer;
import com.groupservice.repository.GroupExpenseRepository;
import com.groupservice.repository.GroupExpenseSplitRepository;
import com.groupservice.repository.GroupMemberRepository;
import com.groupservice.repository.GroupRepository;
import com.groupservice.repository.GroupUserRepository;
import com.groupservice.service.GroupService;

import jakarta.transaction.Transactional;

@Service
public class GroupServiceImpl implements GroupService {

    @Autowired
    private GroupUserRepository groupUserRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private GroupExpenseRepository groupExpenseRepository;

    @Autowired
    private GroupExpenseSplitRepository groupExpenseSplitRepository;

    @Autowired
    private GroupEventProducer groupEventProducer;

    @Override
    @Transactional
    public Group createGroup(CreateGroupRequest request) {
        this.groupUserRepository.findById(request.getCreatedBy())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getCreatedBy()));

        Group group = new Group();
        group.setName(request.getName());
        group.setCreatedBy(request.getCreatedBy());
        group.setCreatedAt(LocalDateTime.now());
        group.setUpdatedAt(LocalDateTime.now());

        Group savedGroup = this.groupRepository.save(group);
        GroupMember groupMember = new GroupMember();
        groupMember.setGroupId(savedGroup.getId());
        groupMember.setUserId(request.getCreatedBy());
        groupMember.setRole("ADMIN");
        groupMember.setJoinedAt(LocalDateTime.now());

        this.groupMemberRepository.save(groupMember);

        return savedGroup;

    }

    @Override
    @Transactional
    public void addMemberToGroup(UUID groupId, AddMemberRequest request) {
        this.groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));
        this.groupUserRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        this.groupMemberRepository.findByGroupIdAndUserId(groupId, request.getUserId())
                .ifPresent(gm -> {
                    throw new RuntimeException("User with id: " + request.getUserId()
                            + " is already a member of the group with id: " + groupId);
                });

        GroupMember groupMember = new GroupMember();
        groupMember.setGroupId(groupId);
        groupMember.setUserId(request.getUserId());
        groupMember.setRole(request.getRole());
        groupMember.setJoinedAt(LocalDateTime.now());

        this.groupMemberRepository.save(groupMember);
    }

    @Override
    @Transactional
    public void createExpense(UUID groupId, CreateExpenseRequest request) throws BadRequestException {

        this.groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        if (!this.groupMemberRepository.existsByGroupIdAndUserId(groupId, request.getPaidBy())) {
            throw new RuntimeException(
                    "User with id: " + request.getPaidBy() + " is not a member of the group with id: " + groupId);
        }

        BigDecimal totalSplit = request.getSplits()
                .stream()
                .map(ExpenseSplitRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalSplit.compareTo(request.getAmount()) != 0) {
            throw new RuntimeException("Split total does not match expense amount");
        }

        GroupExpense expense = new GroupExpense();
        expense.setGroupId(groupId);
        expense.setPaidBy(request.getPaidBy());
        expense.setAmount(request.getAmount());
        expense.setCurrency(request.getCurrency());
        expense.setCategory(request.getCategory());
        expense.setDescription(request.getDescription());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setCreatedAt(LocalDateTime.now());
        expense.setUpdatedAt(LocalDateTime.now());

        GroupExpense savedExpense = groupExpenseRepository.save(expense);

        Set<UUID> splitUsers = new HashSet<>();

        for (ExpenseSplitRequest split : request.getSplits()) {

            if (!this.groupMemberRepository.existsByGroupIdAndUserId(groupId, split.getUserId())) {
                throw new RuntimeException(
                        "User with id: " + split.getUserId() + " is not a member of the group with id: " + groupId);
            }

            if (!splitUsers.add(split.getUserId())) {
                throw new BadRequestException(
                        "Duplicate split detected for user: " + split.getUserId());
            }

            GroupExpenseSplit expenseSplit = new GroupExpenseSplit();
            expenseSplit.setExpenseId(savedExpense.getId());
            expenseSplit.setUserId(split.getUserId());
            expenseSplit.setAmountOwed(split.getAmount());
            expenseSplit.setCreatedAt(LocalDateTime.now());

            groupExpenseSplitRepository.save(expenseSplit);

        }

        List<GroupExpenseCreatedEvent.ExpenseSplitPayload> payloadSplits = request.getSplits().stream()
                .map(split -> new ExpenseSplitPayload(split.getUserId(), split.getAmount()))
                .toList();

        GroupExpenseCreatedEvent event = new GroupExpenseCreatedEvent(
                savedExpense.getId(),
                groupId,
                request.getPaidBy(),
                request.getAmount(),
                request.getCurrency(),
                request.getExpenseDate(),
                payloadSplits
        );

        groupEventProducer.publishExpenseCreated(event);

    }

}
