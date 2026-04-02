package com.groupservice.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.groupservice.dto.AddMemberRequest;
import com.groupservice.dto.CreateExpenseRequest;
import com.groupservice.dto.CreateGroupRequest;
import com.groupservice.dto.GroupResponse;
import com.groupservice.dto.GroupMemberResponse;
import com.groupservice.entity.Group;
import com.groupservice.entity.GroupExpense;
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
        return ResponseEntity.ok(groupService.buildGroupResponse(group));
    }

    @GetMapping("/")
    public ResponseEntity<List<GroupResponse>> getAllGroups(
            @RequestHeader("X-User-Id") UUID userId) {
        List<Group> groups = this.groupService.getGroupsByUserId(userId);
        List<GroupResponse> responses = groups.stream()
                .map(groupService::buildGroupResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getGroupById(@PathVariable UUID groupId) {
        Group group = this.groupService.getGroupById(groupId);
        return ResponseEntity.ok(groupService.buildGroupResponse(group));
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<GroupResponse> updateGroup(
            @PathVariable UUID groupId,
            @RequestHeader("X-User-Id") UUID requestingUserId,
            @RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Group group = this.groupService.updateGroup(groupId, name, requestingUserId);
        return ResponseEntity.ok(groupService.buildGroupResponse(group));
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(
            @PathVariable UUID groupId,
            @RequestHeader("X-User-Id") UUID requestingUserId) {
        this.groupService.deleteGroup(groupId, requestingUserId);
        return ResponseEntity.noContent().build();
    }

    // --- Members ---

    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMemberResponse>> getMembers(@PathVariable UUID groupId) {
        List<GroupMemberResponse> members = this.groupService.getGroupMembers(groupId);
        return ResponseEntity.ok(members);
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<Void> addMember(
            @PathVariable UUID groupId,
            @RequestHeader("X-User-Id") UUID requestingUserId,
            @Valid @RequestBody AddMemberRequest request) {
        this.groupService.addMemberToGroup(groupId, request, requestingUserId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID groupId,
            @PathVariable UUID userId,
            @RequestHeader("X-User-Id") UUID requestingUserId) {
        this.groupService.removeMember(groupId, userId, requestingUserId);
        return ResponseEntity.noContent().build();
    }

    // --- Expenses ---

    @GetMapping("/{groupId}/expenses")
    public ResponseEntity<List<GroupExpense>> getExpenses(@PathVariable UUID groupId) {
        List<GroupExpense> expenses = this.groupService.getGroupExpenses(groupId);
        return ResponseEntity.ok(expenses);
    }

    @PostMapping("/{groupId}/expenses")
    public ResponseEntity<Void> createExpense(
            @PathVariable UUID groupId,
            @Valid @RequestBody CreateExpenseRequest request) throws BadRequestException {
        this.groupService.createExpense(groupId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{groupId}/expenses/{expenseId}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable UUID groupId,
            @PathVariable UUID expenseId,
            @RequestHeader("X-User-Id") UUID requestingUserId) {
        this.groupService.deleteExpense(groupId, expenseId, requestingUserId);
        return ResponseEntity.noContent().build();
    }

}
