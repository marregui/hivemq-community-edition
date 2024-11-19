/*
 * Copyright 2019-present HiveMQ GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hivemq.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class SecurityConfigurationService {

    public static final boolean ALLOW_SERVER_ASSIGNED_CLIENT_ID_DEFAULT = true;
    public static final boolean VALIDATE_UTF_8_DEFAULT = true;
    public static final boolean PAYLOAD_FORMAT_VALIDATION_DEFAULT = false;
    public static final boolean ALLOW_REQUEST_PROBLEM_INFORMATION_DEFAULT = true;

    private static final Logger log = LoggerFactory.getLogger(SecurityConfigurationService.class);

    private final AtomicBoolean allowServerAssignedClientId =
            new AtomicBoolean(ALLOW_SERVER_ASSIGNED_CLIENT_ID_DEFAULT);
    private final AtomicBoolean validateUTF8 = new AtomicBoolean(VALIDATE_UTF_8_DEFAULT);
    private final AtomicBoolean payloadFormatValidation = new AtomicBoolean(PAYLOAD_FORMAT_VALIDATION_DEFAULT);
    private final AtomicBoolean allowRequestProblemInformation =
            new AtomicBoolean(ALLOW_REQUEST_PROBLEM_INFORMATION_DEFAULT);

    public boolean allowServerAssignedClientId() {
        return allowServerAssignedClientId.get();
    }

    public boolean validateUTF8() {
        return validateUTF8.get();
    }

    public boolean payloadFormatValidation() {
        return payloadFormatValidation.get();
    }

    public boolean allowRequestProblemInformation() {
        return allowRequestProblemInformation.get();
    }

    public void setValidateUTF8(final boolean validateUTF8) {
        log.debug("Setting validate UTF-8 to {}", validateUTF8);
        this.validateUTF8.set(validateUTF8);
    }

    public void setPayloadFormatValidation(final boolean payloadFormatValidation) {
        log.debug("Setting payload format validation to {}", payloadFormatValidation);
        this.payloadFormatValidation.set(payloadFormatValidation);
    }

    public void setAllowServerAssignedClientId(final boolean allowServerAssignedClientId) {
        log.debug("Setting allow server assigned client identifier to {}", allowServerAssignedClientId);
        this.allowServerAssignedClientId.set(allowServerAssignedClientId);
    }

    public void setAllowRequestProblemInformation(final boolean allowRequestProblemInformation) {
        log.debug("Setting allow-problem-information to {}", allowRequestProblemInformation);
        this.allowRequestProblemInformation.set(allowRequestProblemInformation);
    }
}
