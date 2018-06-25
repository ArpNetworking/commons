/*
 * Copyright 2016 Groupon.com
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
package com.arpnetworking.commons.jackson.databind.introspect;

import com.arpnetworking.commons.builder.Builder;
import com.arpnetworking.commons.jackson.databind.annotation.JsonIgnoreBuilder;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * Jackson {@link com.fasterxml.jackson.databind.AnnotationIntrospector} that injects a
 * {@link com.fasterxml.jackson.databind.annotation.JsonDeserialize} annotation for classes that contain a
 * {@link com.arpnetworking.commons.builder.Builder} inner class.
 *
 * Dependencies:
 * <ul>
 *     <li>com.fasterxml.jackson.core:jackson-databind</li>
 * </ul>
 *
 * @author Gil Markham (gil at groupon dot com)
 */
public final class BuilderAnnotationIntrospector extends NopAnnotationIntrospector {

    @Override
    public @Nullable Class<?> findPOJOBuilder(final AnnotatedClass ac) {
        final Class<?> pojoClass = ac.getRawType();
        final Class<?>[] declaredClasses = pojoClass.getDeclaredClasses();
        for (final Class<?> declaredClass : declaredClasses) {
            if (!Modifier.isAbstract(declaredClass.getModifiers())
                    && Builder.class.isAssignableFrom(declaredClass)
                    && declaredClass.getAnnotation(JsonIgnoreBuilder.class) == null) {
                return declaredClass;
            }
        }
        return null;
    }

    @Override
    public JsonPOJOBuilder.Value findPOJOBuilderConfig(final AnnotatedClass ac) {
        return new JsonPOJOBuilder.Value(new MyPojoBuilder());
    }

    private static final long serialVersionUID = 4340729558194130927L;

    /**
     * Constructable implementation of JsonPOJOBuilder annotation.
     */
    public static class MyPojoBuilder implements JsonPOJOBuilder {

        @Override
        public String buildMethodName() {
            return _methodName;
        }

        @Override
        public String withPrefix() {
            return _prefix;
        }

        @Override
        public String toString() {
            return buildMethodName() + " " + withPrefix();
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return JsonPOJOBuilder.class;
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof MyPojoBuilder)) {
                return false;
            }
            final MyPojoBuilder otherPojoBuilder = (MyPojoBuilder) other;
            return Objects.equals(buildMethodName(), otherPojoBuilder.buildMethodName())
                    && Objects.equals(withPrefix(), otherPojoBuilder.withPrefix());
        }

        @Override
        public int hashCode() {
            return Objects.hash(buildMethodName(), withPrefix());
        }

        /**
         * Public constructor.
         */
        public MyPojoBuilder() {
            this("build", "set");
        }

        /* package private */ MyPojoBuilder(final String methodName, final String prefix) {
            _methodName = methodName;
            _prefix = prefix;
        }

        private final String _methodName;
        private final String _prefix;
    }
}
