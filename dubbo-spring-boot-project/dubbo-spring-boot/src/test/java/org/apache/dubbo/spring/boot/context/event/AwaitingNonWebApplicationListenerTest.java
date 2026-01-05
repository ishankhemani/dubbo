/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.spring.boot.context.event;

import org.apache.dubbo.config.bootstrap.DubboBootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link AwaitingNonWebApplicationListener} tests
 */
class AwaitingNonWebApplicationListenerTest {

    @BeforeEach
    void before() {
        DubboBootstrap.reset();
    }

    @AfterEach
    void after() {
        DubboBootstrap.reset();
    }

    /**
     * Single non-web context:
     * await() should be released on shutdown
     */
    @Test
    void testSingleContextNonWebApplication() {

        ConfigurableApplicationContext context =
                new SpringApplicationBuilder(Object.class)
                        .web(WebApplicationType.NONE)
                        .run();

        AtomicBoolean awaited =
                new AwaitingNonWebApplicationListener().getAwaited();

        assertFalse(awaited.get(), "awaited must be false before shutdown");

        context.close();

        assertTrue(awaited.get(), "awaited must be true after shutdown");
    }

    /**
     * Parent-child non-web context:
     * parent ApplicationReadyEvent MUST NOT trigger await()
     */
    @Test
    void testMultipleContextNonWebApplication() {

        ConfigurableApplicationContext context =
                new SpringApplicationBuilder(Object.class)
                        .parent(Object.class)
                        .web(WebApplicationType.NONE)
                        .run();

        AtomicBoolean awaited =
                new AwaitingNonWebApplicationListener().getAwaited();

        assertFalse(awaited.get(),
                "awaited must remain false after parent context ready");

        context.close();
        assertFalse(awaited.get(),
                "awaited must remain false when no Dubbo ApplicationModel is present");

    }
}
