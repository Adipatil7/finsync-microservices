package com.groupservice.service;

import java.util.List;
import java.util.UUID;

import org.apache.coyote.BadRequestException;

import com.groupservice.dto.AddMemberRequest;
import com.groupservice.dto.CreateExpenseRequest;
import com.groupservice.dto.CreateGroupRequest;
import com.groupservice.dto.GroupResponse;
import com.groupservice.entity.Group;
import com.groupservice.entity.GroupExpense;
import com.groupservice.dto.GroupMemberResponse;

public interface GroupService {

    Group createGroup(CreateGroupRequest request);

    void addMemberToGroup(UUID groupId, AddMemberRequest request, UUID requestingUserId);

    void createExpense(UUID groupId, CreateExpenseRequest request) throws BadRequestException;

    List<Group> getGroupsByUserId(UUID userId);

    Group getGroupById(UUID groupId);

    List<GroupMemberResponse> getGroupMembers(UUID groupId);

    List<GroupExpense> getGroupExpenses(UUID groupId);

    void deleteGroup(UUID groupId, UUID requestingUserId);

    Group updateGroup(UUID groupId, String name, UUID requestingUserId);

    void removeMember(UUID groupId, UUID userId, UUID requestingUserId);

    void deleteExpense(UUID groupId, UUID expenseId, UUID requestingUserId);

    GroupResponse buildGroupResponse(Group group);

}
