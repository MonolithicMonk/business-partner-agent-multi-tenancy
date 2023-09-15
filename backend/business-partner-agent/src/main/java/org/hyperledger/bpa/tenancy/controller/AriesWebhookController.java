/*
 * Copyright (c) 2020-2022 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository at
 * https://github.com/hyperledger-labs/business-partner-agent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hyperledger.bpa.tenancy.controller;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.aries.webhook.TenantAwareEventHandler;
import org.hyperledger.bpa.tenancy.security.roles.RolesandPermissions;

import io.micronaut.http.HttpRequest;
import java.util.List;

/**
 * Handles incoming aca-py webhook events
 */
@Slf4j
@Hidden
@Tag(name = "Aries Webhook")
@Controller
@Secured(SecurityRule.IS_AUTHENTICATED)
@ExecuteOn(TaskExecutors.IO)
public class AriesWebhookController {

    public static final String WEBHOOK_CONTROLLER_PATH = "/log/topic";

    @Inject
    List<TenantAwareEventHandler> handlers;

    @Secured({ RolesandPermissions.ROLE_TENANT_ADMIN })
    @Post(WEBHOOK_CONTROLLER_PATH + "/{eventType}")
    public void logEvent(
            @PathVariable String eventType,
            @Body String eventBody,
            HttpRequest<?> request) {
        log.info("Webhook received, type: {}", eventType);
        String walletId = request.getHeaders().get("x-wallet-id");
        handlers.forEach(eventHandler -> eventHandler.handleEvent(walletId, eventType, eventBody));
    }
}