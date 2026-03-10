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
import com.groupservice.dto.GroupResponse;
import com.groupservice.dto.GroupMemberResponse;
import com.groupservice.entity.Group;
import com.groupservice.entity.GroupExpense;
import com.groupservice.entity.GroupExpenseSplit;
import com.groupservice.entity.GroupMember;
import com.groupservice.entity.GroupUser;
import com.groupservice.kafka.GroupEventProducer;
import com.groupservice.repository.GroupExpenseRepository;
import com.groupservice.repository.GroupExpenseSplitRepository;
import com.groupservice.repository.GroupMemberRepository;
import com.groupservice.repository.GroupRepository;
import com.groupservice.repository.GroupUserRepository;
import com.groupservice.service.GroupService;
import com.groupservice.split.SplitStrategy;
import com.groupservice.split.SplitStrategyFactory;

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

        @Autowired
        private SplitStrategyFactory splitStrategyFactory;

        @Override
        @Transactional
        public Group createGroup(CreateGroupRequest request) {
                this.groupUserRepository.findById(request.getCreatedBy())
                                .orElseThrow(() -> new RuntimeException(
                                                "User not found with id: " + request.getCreatedBy()));

                Group group = new Group();
                group.setName(request.getName());
                group.setDescription(request.getDescription());
                group.setCurrency(request.getCurrency() != null ? request.getCurrency() : "INR");
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
                                .orElseThrow(() -> new RuntimeException(
                                                "User not found with id: " + request.getUserId()));

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
                                        "User with id: " + request.getPaidBy()
                                                        + " is not a member of the group with id: " + groupId);
                }

                GroupExpense expense = new GroupExpense();
                expense.setGroupId(groupId);
                expense.setPaidBy(request.getPaidBy());
                expense.setAmount(request.getAmount());
                expense.setCurrency(request.getCurrency());
                expense.setCategory(request.getCategory());
                expense.setDescription(request.getDescription());
                expense.setExpenseDate(request.getExpenseDate());
                expense.setSplitType(request.getSplitType());
                expense.setCreatedAt(LocalDateTime.now());
                expense.setUpdatedAt(LocalDateTime.now());

                GroupExpense savedExpense = groupExpenseRepository.save(expense);

                Set<UUID> groupMembers = groupMemberRepository.findUserIdsByGroupId(groupId);
                Set<UUID> seenUsers = new HashSet<>();

                for (ExpenseSplitRequest split : request.getSplits()) {

                        if (!groupMembers.contains(split.getUserId())) {
                                throw new RuntimeException(
                                                "User with id " + split.getUserId() + " is not a member of group "
                                                                + groupId);
                        }

                        if (!seenUsers.add(split.getUserId())) {
                                throw new BadRequestException(
                                                "Duplicate split detected for user " + split.getUserId());
                        }
                }

                SplitStrategy strategy = this.splitStrategyFactory.getStrategy(request.getSplitType());

                List<GroupExpenseSplit> splits = strategy.calculateSplits(request, savedExpense.getId());

                groupExpenseSplitRepository.saveAll(splits);

                List<GroupExpenseCreatedEvent.ExpenseSplitPayload> payloadSplits = splits.stream()
                                .map(split -> new ExpenseSplitPayload(
                                                split.getUserId(),
                                                split.getAmountOwed()))
                                .toList();

                GroupExpenseCreatedEvent event = new GroupExpenseCreatedEvent(
                                savedExpense.getId(),
                                groupId,
                                request.getPaidBy(),
                                request.getAmount(),
                                request.getCurrency(),
                                request.getExpenseDate(),
                                payloadSplits);

                groupEventProducer.publishExpenseCreated(event);

        }

        @Override
        public List<Group> getGroupsByUserId(UUID userId) {
                List<GroupMember> memberships = this.groupMemberRepository.findByUserId(userId);
                List<UUID> groupIds = memberships.stream().map(GroupMember::getGroupId).toList();
                return this.groupRepository.findAllById(groupIds);
        }

        @Override
        public Group getGroupById(UUID groupId) {
                return this.groupRepository.findById(groupId)
                                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));
        }

        @Override
        public List<GroupMemberResponse> getGroupMembers(UUID groupId) {
                this.groupRepository.findById(groupId)
                                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));
                List<GroupMember> members = this.groupMemberRepository.findByGroupId(groupId);
                return members.stream().map(member -> {
                        String name = this.groupUserRepository.findById(member.getUserId())
                                        .map(GroupUser::getName)
                                        .orElse("Unknown User");
                        return GroupMemberResponse.from(member, name);
                }).toList();
        }

        @Override
        public List<GroupExpense> getGroupExpenses(UUID groupId) {
                this.groupRepository.findById(groupId)
                                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));
                return this.groupExpenseRepository.findByGroupId(groupId);
        }

        @Override
        @Transactional
        public void deleteGroup(UUID groupId) {
                this.groupRepository.findById(groupId)
                                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));
                this.groupRepository.deleteById(groupId);
        }

        @Override
        @Transactional
        public Group updateGroup(UUID groupId, String name) {
                Group group = this.groupRepository.findById(groupId)
                                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));
                group.setName(name);
                group.setUpdatedAt(LocalDateTime.now());
                return this.groupRepository.save(group);
        }

        @Override
        @Transactional
        public void removeMember(UUID groupId, UUID userId) {
                this.groupRepository.findById(groupId)
                                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));
                this.groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                                .orElseThrow(() -> new RuntimeException(
                                                "User with id: " + userId + " is not a member of group: " + groupId));
                this.groupMemberRepository.deleteByGroupIdAndUserId(groupId, userId);
        }

        @Override
        public GroupResponse buildGroupResponse(Group group) {
                long memberCount = this.groupMemberRepository.countByGroupId(group.getId());
                BigDecimal totalSpent = this.groupExpenseRepository.sumAmountByGroupId(group.getId());
                return GroupResponse.from(group, (int) memberCount, totalSpent);
        }

}
