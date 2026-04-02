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
import com.groupservice.enums.GroupRole;
import com.groupservice.exception.AccessDeniedException;
import com.groupservice.kafka.GroupEventProducer;
import com.groupservice.repository.GroupExpenseRepository;
import com.groupservice.repository.GroupExpenseSplitRepository;
import com.groupservice.repository.GroupMemberRepository;
import com.groupservice.repository.GroupRepository;
import com.groupservice.repository.GroupUserRepository;
import com.groupservice.service.GroupAuthorizationService;
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

        @Autowired
        private GroupAuthorizationService authService;

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
                groupMember.setRole(GroupRole.ADMIN.name());
                groupMember.setJoinedAt(LocalDateTime.now());

                this.groupMemberRepository.save(groupMember);

                return savedGroup;

        }

        @Override
        @Transactional
        public void addMemberToGroup(UUID groupId, AddMemberRequest request, UUID requestingUserId) {
                this.groupRepository.findById(groupId)
                                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

                // RBAC: Only ADMIN can add members
                authService.requireAdmin(groupId, requestingUserId);

                this.groupUserRepository.findById(request.getUserId())
                                .orElseThrow(() -> new RuntimeException(
                                                "User not found with id: " + request.getUserId()));

                this.groupMemberRepository.findByGroupIdAndUserId(groupId, request.getUserId())
                                .ifPresent(gm -> {
                                        throw new RuntimeException("User with id: " + request.getUserId()
                                                        + " is already a member of the group with id: " + groupId);
                                });

                // Validate and default role
                GroupRole validatedRole = GroupRole.fromString(request.getRole());

                GroupMember groupMember = new GroupMember();
                groupMember.setGroupId(groupId);
                groupMember.setUserId(request.getUserId());
                groupMember.setRole(validatedRole.name());
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
        public void deleteGroup(UUID groupId, UUID requestingUserId) {
                this.groupRepository.findById(groupId)
                                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

                // RBAC: Only ADMIN can delete group
                authService.requireAdmin(groupId, requestingUserId);

                this.groupRepository.deleteById(groupId);
        }

        @Override
        @Transactional
        public Group updateGroup(UUID groupId, String name, UUID requestingUserId) {
                Group group = this.groupRepository.findById(groupId)
                                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

                // RBAC: Only ADMIN can update group
                authService.requireAdmin(groupId, requestingUserId);

                group.setName(name);
                group.setUpdatedAt(LocalDateTime.now());
                return this.groupRepository.save(group);
        }

        @Override
        @Transactional
        public void removeMember(UUID groupId, UUID userId, UUID requestingUserId) {
                this.groupRepository.findById(groupId)
                                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

                // RBAC: Only ADMIN can remove members
                authService.requireAdmin(groupId, requestingUserId);

                GroupMember member = this.groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                                .orElseThrow(() -> new RuntimeException(
                                                "User with id: " + userId + " is not a member of group: " + groupId));

                // Safety: Prevent removing the last ADMIN
                if (GroupRole.ADMIN.name().equals(member.getRole()) && authService.isLastAdmin(groupId)) {
                        throw new RuntimeException(
                                        "Cannot remove the last ADMIN from group " + groupId
                                                        + ". Promote another member to ADMIN first.");
                }

                this.groupMemberRepository.deleteByGroupIdAndUserId(groupId, userId);
        }

        @Override
        @Transactional
        public void deleteExpense(UUID groupId, UUID expenseId, UUID requestingUserId) {
                this.groupRepository.findById(groupId)
                                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

                GroupExpense expense = this.groupExpenseRepository.findById(expenseId)
                                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + expenseId));

                if (!expense.getGroupId().equals(groupId)) {
                        throw new RuntimeException("Expense " + expenseId + " does not belong to group " + groupId);
                }

                // RBAC: ADMIN can delete any expense, MEMBER can delete only their own
                GroupMember member = this.groupMemberRepository.findByGroupIdAndUserId(groupId, requestingUserId)
                                .orElseThrow(() -> new AccessDeniedException(
                                                "User " + requestingUserId + " is not a member of group " + groupId));

                boolean isAdmin = GroupRole.ADMIN.name().equals(member.getRole());
                boolean isOwner = expense.getPaidBy().equals(requestingUserId);

                if (!isAdmin && !isOwner) {
                        throw new AccessDeniedException(
                                        "User " + requestingUserId
                                                        + " is not authorized to delete this expense. Only ADMIN or the expense creator can delete.");
                }

                this.groupExpenseSplitRepository.deleteByExpenseId(expenseId);
                this.groupExpenseRepository.deleteById(expenseId);
        }

        @Override
        public GroupResponse buildGroupResponse(Group group) {
                long memberCount = this.groupMemberRepository.countByGroupId(group.getId());
                BigDecimal totalSpent = this.groupExpenseRepository.sumAmountByGroupId(group.getId());
                return GroupResponse.from(group, (int) memberCount, totalSpent);
        }

}
