package com.github.son_daehyeon.domain.project.$instance.$matrix.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.son_daehyeon.common.api.dto.response.ApiResponse;
import com.github.son_daehyeon.common.docs.swagger.SwaggerConfig;
import com.github.son_daehyeon.common.validation.custom.Enum;
import com.github.son_daehyeon.domain.project.$instance.$matrix.constant.TimeUnit;
import com.github.son_daehyeon.domain.project.$instance.$matrix.dto.response.InstanceCurrentMatrixResponse;
import com.github.son_daehyeon.domain.project.$instance.$matrix.dto.response.InstanceMatrixResponse;
import com.github.son_daehyeon.domain.project.$instance.$matrix.service.InstanceMatrixService;
import com.github.son_daehyeon.domain.user.schema.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/project/{projectId}/instance/{instanceId}/matrix")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "[Project] [Instance] Matrix")
@SecurityRequirement(name = SwaggerConfig.SWAGGER_AUTH)
public class InstanceMatrixController {

    private final InstanceMatrixService instanceMatrixService;

    @GetMapping("/current")
    @Operation(summary = "인스턴스 현재 성능")
    public ApiResponse<InstanceCurrentMatrixResponse> getCurrentMatrix(@PathVariable String projectId, @PathVariable String instanceId, @AuthenticationPrincipal User user) {

        return ApiResponse.ok(instanceMatrixService.getCurrentMatrix(projectId, instanceId, user));
    }

    @GetMapping()
    @Operation(summary = "인스턴스 지속 성능")
    public ApiResponse<InstanceMatrixResponse> getMatrix(@PathVariable String projectId, @PathVariable String instanceId, @RequestParam(defaultValue = "HOUR") @Enum(enumClass = TimeUnit.class) String timeUnit, @AuthenticationPrincipal User user) {

        return ApiResponse.ok(instanceMatrixService.getMatrix(projectId, instanceId, TimeUnit.valueOf(timeUnit), user));
    }
}
