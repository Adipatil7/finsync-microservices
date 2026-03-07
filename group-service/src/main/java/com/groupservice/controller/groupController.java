package com.groupservice.controller;

import java.util.UUID;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.groupservice.dto.AddMemberRequest;
import com.groupservice.dto.CreateExpenseRequest;
import com.groupservice.dto.CreateGroupRequest;
import com.groupservice.dto.GroupResponse;
import com.groupservice.entity.Group;
import com.groupservice.service.GroupService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/groups")
public class groupController {

    @Autowired
    private GroupService groupService;
    
    @PostMapping("/")
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        
        Group group = this.groupService.createGroup(request);

        return ResponseEntity.ok(GroupResponse.from(group));

    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<Void> addMember(
        @PathVariable UUID groupId,
        @Valid @RequestBody AddMemberRequest request
    ){

        this.groupService.addMemberToGroup(groupId, request);
        return ResponseEntity.ok().build();

    }

    @PostMapping("/{groupId}/expenses")
    public ResponseEntity<Void> createExpense(
        @PathVariable UUID groupId,
        @Valid @RequestBody CreateExpenseRequest request
    ) throws BadRequestException{

        this.groupService.createExpense(groupId, request);
        return ResponseEntity.ok().build();

    }

}
