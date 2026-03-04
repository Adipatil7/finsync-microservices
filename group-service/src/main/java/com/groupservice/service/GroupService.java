package com.groupservice.service;

import java.util.UUID;

import org.apache.coyote.BadRequestException;

import com.groupservice.dto.AddMemberRequest;
import com.groupservice.dto.CreateExpenseRequest;
import com.groupservice.dto.CreateGroupRequest;
import com.groupservice.entity.Group;

public interface GroupService {

    Group createGroup(CreateGroupRequest request);

    void addMemberToGroup(UUID groupId, AddMemberRequest request);

    void createExpense(UUID groupId, CreateExpenseRequest request) throws BadRequestException;

}
