/*
 * Copyright 2022 Creek Contributors (https://github.com/creek-service)
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

package org.creek.test.service.context;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.creek.api.base.type.temporal.AccurateClock;
import org.creek.api.base.type.temporal.Clock;
import org.creek.api.platform.metadata.ComponentInput;
import org.creek.api.platform.metadata.ServiceDescriptor;
import org.creek.api.service.context.CreekContext;
import org.creek.api.service.context.CreekServices;
import org.creek.api.service.extension.CreekExtensionOptions;
import org.creek.test.java.nine.service.extension.JavaNineExtensionBuilder;
import org.creek.test.java.nine.service.extension.JavaNineExtensionBuilder2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreekServicesTest {

    @Mock private ServiceDescriptor serviceDescriptor;

    @BeforeEach
    void setUp() {
        when(serviceDescriptor.name()).thenReturn("the-service");
        when(serviceDescriptor.resources()).thenCallRealMethod();
    }

    @Test
    void shouldExposeDefaultClock() {
        // When:
        final CreekContext ctx = CreekServices.context(serviceDescriptor);

        // Then:
        assertThat(ctx.clock(), is(instanceOf(AccurateClock.class)));
    }

    @Test
    void shouldExposeSpecificClock() {
        // Given:
        final Clock clock = mock(Clock.class);
        // When:
        final CreekContext ctx = CreekServices.builder(serviceDescriptor).with(clock).build();

        // Then:
        assertThat(ctx.clock(), is(sameInstance(clock)));
    }

    @Test
    void shouldExposeExtensions() {
        // When:
        final CreekContext ctx = CreekServices.context(serviceDescriptor);

        // Then:
        assertThat(
                ctx.extension(JavaNineExtensionBuilder.Extension.class),
                is(instanceOf(JavaNineExtensionBuilder.Extension.class)));
        assertThat(
                ctx.extension(JavaNineExtensionBuilder2.Extension.class),
                is(instanceOf(JavaNineExtensionBuilder2.Extension.class)));
    }

    @Test
    void shouldThrowIfOptionsNotHandledByAnyExtension() {
        // Given:
        final CreekServices.Builder builder = CreekServices.builder(serviceDescriptor);

        // Then:
        assertThrows(
                IllegalArgumentException.class,
                () -> builder.with(new UnhandledExtensionOptions()));
    }

    @Test
    void shouldNotThrowIfOptionsHandled() {
        // Given:
        final CreekServices.Builder builder = CreekServices.builder(serviceDescriptor);

        // Then: does not throw.
        builder.with(new JavaNineExtensionBuilder.Options()).build();
    }

    @Test
    void shouldThrowOnUnhandledResource() {
        // Given:
        when(serviceDescriptor.inputs()).thenReturn(List.of(new UnhandledResourceDef()));

        // Then:
        final Exception e =
                assertThrows(Exception.class, () -> CreekServices.builder(serviceDescriptor));

        // Then:
        assertThat(
                e.getMessage(),
                startsWith("Component defines resources for which no extension is installed."));
    }

    @Test
    void shouldNotThrowIfAllResourcesHandled() {
        // Given:
        when(serviceDescriptor.inputs()).thenReturn(List.of(new JavaNineExtensionBuilder.Input()));
        when(serviceDescriptor.internals())
                .thenReturn(List.of(new JavaNineExtensionBuilder2.Internal()));
        when(serviceDescriptor.outputs())
                .thenReturn(List.of(new JavaNineExtensionBuilder2.Output()));

        // When:
        final CreekContext ctx = CreekServices.context(serviceDescriptor);

        // Then:
        assertThat(ctx, is(notNullValue()));
    }

    private static final class UnhandledExtensionOptions implements CreekExtensionOptions {}

    private static final class UnhandledResourceDef implements ComponentInput {}
}
