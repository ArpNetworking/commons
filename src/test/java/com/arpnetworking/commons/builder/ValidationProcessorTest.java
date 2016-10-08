/**
 * Copyright 2016 Inscope Metrics Inc.
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
package com.arpnetworking.commons.builder;

import com.arpnetworking.commons.builder.annotations.SkipValidationProcessor;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import net.sf.oval.ConstraintViolation;
import net.sf.oval.constraint.NotNull;
import net.sf.oval.constraint.NotNullCheck;
import net.sf.oval.constraint.ValidateWithMethod;
import net.sf.oval.context.FieldContext;
import net.sf.oval.exception.ConstraintsViolatedException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nonnull;

/**
 * Tests for the <code>ValidationProcessor</code>.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public final class ValidationProcessorTest {

    @Test
    public void testAcceptRejectOvalBuilder() throws NotFoundException {
        final ValidationProcessor processor = new ValidationProcessor();
        final ClassPool classPool = createClassPool();
        Assert.assertFalse(processor.accept(classPool.get(
                "com.arpnetworking.commons.builder.OvalBuilder")));
    }

    @Test
    public void testAcceptRejectSkipValidationProcessor() throws NotFoundException {
        final ValidationProcessor processor = new ValidationProcessor();
        final ClassPool classPool = createClassPool();
        Assert.assertFalse(processor.accept(classPool.get(
                "com.arpnetworking.commons.builder.ValidationProcessorTest$SkippedBuilder")));
    }

    @Test
    public void testAcceptRejectByDefault() throws NotFoundException {
        final ValidationProcessor processor = new ValidationProcessor();
        final ClassPool classPool = createClassPool();
        Assert.assertFalse(processor.accept(classPool.get(
                "com.arpnetworking.commons.builder.ValidationProcessorTest")));
    }

    @Test
    public void testAcceptOvalBuilderDescendents() throws NotFoundException {
        final ValidationProcessor processor = new ValidationProcessor();
        final ClassPool classPool = createClassPool();
        Assert.assertTrue(processor.accept(classPool.get(
                "com.arpnetworking.commons.builder.ValidationProcessorTest$ImmediateBuilder")));
        Assert.assertTrue(processor.accept(classPool.get(
                "com.arpnetworking.commons.builder.ValidationProcessorTest$DescendentBuilder")));
    }

    @Test(expected = RuntimeException.class)
    public void testAcceptAnnotationLoadFailure() throws NotFoundException, ClassNotFoundException {
        final ValidationProcessor processor = new ValidationProcessor();
        final CtClass ctClass = Mockito.mock(CtClass.class);
        Mockito.doThrow(new ClassNotFoundException()).when(ctClass).getAnnotations();
        processor.accept(ctClass);
    }

    @Test(expected = RuntimeException.class)
    public void testAcceptParentLoadFailure() throws NotFoundException, ClassNotFoundException {
        final ValidationProcessor processor = new ValidationProcessor();
        final CtClass ctClass = Mockito.mock(CtClass.class);
        Mockito.when(ctClass.getAnnotations()).thenReturn(new Object[0]);
        Mockito.doThrow(new NotFoundException("Mocked exception")).when(ctClass).getSuperclass();
        processor.accept(ctClass);
    }

    @Test
    public void testProcessComparisonPojo() throws Exception {
        // Assert that no validation happens before processing
        final ExamplePojo pojo = new ComparisonBuilder().setValue(null).build();
        Assert.assertNull(pojo.getValue());

        // Process the example builder class
        final ValidationProcessor processor = new ValidationProcessor();
        final ClassPool classPool = createClassPool();
        final CtClass exampleCtClass = classPool.get(
                "com.arpnetworking.commons.builder.ValidationProcessorTest$ComparisonBuilder");

        exampleCtClass.defrost();
        processor.process(exampleCtClass);
        exampleCtClass.defrost();
        exampleCtClass.setName("com.arpnetworking.commons.builder.ValidationProcessorTest$ProcessedComparisonBuilder");
        exampleCtClass.getClassFile().compact();
        exampleCtClass.rebuildClassFile();

        // Output the class file for debugging
        writeClassFile(exampleCtClass, "ValidationProcessorTest.ProcessedComparisonBuilder.class");

        // Assert that validation happens after processing
        @SuppressWarnings("unchecked")
        final OvalBuilder<ExamplePojo> processedBuilder = (OvalBuilder<ExamplePojo>) exampleCtClass.toClass().newInstance();
        try {
            processedBuilder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, NotNullCheck.class, null, "_value", processedBuilder);
        }
        processedBuilder.getClass().getMethod("setValue", Object.class).invoke(processedBuilder, "Foo");
        final ExamplePojo validatedPojo = processedBuilder.build();
        Assert.assertEquals("Foo", validatedPojo.getValue());
    }

    @Test
    public void testProcessNoChangePojo() throws Exception {
        // Assert that validation happens before processing (reflectively)
        final NoChangeBuilder unmodifiedBuilder = new NoChangeBuilder();
        try {
            unmodifiedBuilder.setValue(null).build();
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, NotNullCheck.class, null, "_value", unmodifiedBuilder);
        }
        final ExamplePojo unmodifiedValidatedPojo = unmodifiedBuilder.setValue("Foo").build();
        Assert.assertEquals("Foo", unmodifiedValidatedPojo.getValue());

        // Process the example builder class
        final ValidationProcessor processor = new ValidationProcessor();
        final ClassPool classPool = createClassPool();
        final CtClass exampleCtClass = classPool.get(
                "com.arpnetworking.commons.builder.ValidationProcessorTest$NoChangeBuilder");

        exampleCtClass.defrost();
        processor.process(exampleCtClass);
        exampleCtClass.defrost();
        exampleCtClass.setName("com.arpnetworking.commons.builder.ValidationProcessorTest$ProcessedNoChangeBuilder");
        exampleCtClass.getClassFile().compact();
        exampleCtClass.rebuildClassFile();

        // Output the class file for debugging
        writeClassFile(exampleCtClass, "ValidationProcessorTest.ProcessedNoChangeBuilder.class");

        // Assert that validation _still_ happens after processing
        @SuppressWarnings("unchecked")
        final OvalBuilder<ExamplePojo> processedBuilder = (OvalBuilder<ExamplePojo>) exampleCtClass.toClass().newInstance();
        try {
            processedBuilder.build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            assertViolation(e, NotNullCheck.class, null, "_value", processedBuilder);
        }
        processedBuilder.getClass().getMethod("setValue", Object.class).invoke(processedBuilder, "Foo");
        final ExamplePojo validatedPojo = processedBuilder.build();
        Assert.assertEquals("Foo", validatedPojo.getValue());
    }

    @Test
    @SuppressFBWarnings("NP_NULL_PARAM_DEREF_ALL_TARGETS_DANGEROUS")
    public void testProcessNoConstraintsPojo() throws Exception {
        // Assert that no validation happens before processing
        @SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
        final ExamplePojo pojo = new NoConstraintsBuilder().setValue(null).build();

        // Process the example builder class
        final ValidationProcessor processor = new ValidationProcessor();
        final ClassPool classPool = createClassPool();
        final CtClass exampleCtClass = classPool.get(
                "com.arpnetworking.commons.builder.ValidationProcessorTest$NoConstraintsBuilder");

        exampleCtClass.defrost();
        processor.process(exampleCtClass);
        exampleCtClass.defrost();
        exampleCtClass.setName("com.arpnetworking.commons.builder.ValidationProcessorTest$ProcesNoConstraintsBuilder");
        exampleCtClass.getClassFile().compact();
        exampleCtClass.rebuildClassFile();

        // Output the class file for debugging
        writeClassFile(exampleCtClass, "ValidationProcessorTest.ProcessedNoConstraintsBuilder.class");

        // Assert that no validation happens after processing
        @SuppressWarnings("unchecked")
        final OvalBuilder<ExamplePojo> builder = (OvalBuilder<ExamplePojo>) exampleCtClass.toClass().newInstance();
        final ExamplePojo postProcessedPojo = builder.build();
        Assert.assertNull(postProcessedPojo.getValue());
    }

    @Test
    @SuppressFBWarnings("NP_NULL_PARAM_DEREF_ALL_TARGETS_DANGEROUS")
    public void testProcessUnprocessedParentPojo() throws Exception {
        // Assert that validation fails reflectively before processing
        try {
            new UnprocessedParentBuilder()
                    .setValue(null)
                    .setOtherValue(null)
                    .build();
            Assert.fail("Expected exception not thrown");
        } catch (final ConstraintsViolatedException e) {
            Assert.assertEquals(2, e.getConstraintViolations().length);
        }

        // Assert that validation also succeeds reflectively before processing
        new UnprocessedParentBuilder()
                .setValue("Foo")
                .setOtherValue("Bar")
                .build();

        // Process the example builder class
        final ValidationProcessor processor = new ValidationProcessor();
        final ClassPool classPool = createClassPool();
        final CtClass exampleCtClass = classPool.get(
                "com.arpnetworking.commons.builder.ValidationProcessorTest$UnprocessedParentBuilder");

        exampleCtClass.defrost();
        processor.process(exampleCtClass);
        exampleCtClass.defrost();
        exampleCtClass.setName("com.arpnetworking.commons.builder.ValidationProcessorTest$ProcesUnprocessedParentBuilder");
        exampleCtClass.getClassFile().compact();
        exampleCtClass.rebuildClassFile();

        // Output the class file for debugging
        writeClassFile(exampleCtClass, "ValidationProcessorTest.ProcessedUnprocessedParentBuilder.class");

        // Assert that no validation happens after processing
        @SuppressWarnings("unchecked")
        final OvalBuilder<ExamplePojo> builder = (OvalBuilder<ExamplePojo>) exampleCtClass.toClass().newInstance();
        try {
            builder.build();
        } catch (final ConstraintsViolatedException e) {
            // The processed class generates a violation both through the
            // injected code and when analyzed reflectively because its
            // immediate parent was not processed. That parent also generates
            // a single violation via relfection. Total three violations.
            Assert.assertEquals(3, e.getConstraintViolations().length);
        }
        builder.getClass().getMethod("setValue", Object.class).invoke(builder, "Foo");
        builder.getClass().getMethod("setOtherValue", Object.class).invoke(builder, "Bar");
        final ExamplePojo pojo = builder.build();
        Assert.assertNotNull(pojo.getValue());
    }

    @Test(expected = RuntimeException.class)
    public void testProcessFailure() throws Exception {
        // Process the example builder class
        final ValidationProcessor processor = new ValidationProcessor();
        final ClassPool classPool = createClassPool();
        final CtClass exampleCtClass = classPool.get(
                "com.arpnetworking.commons.builder.ValidationProcessorTest$FrozenBuilder");
        exampleCtClass.freeze();

        processor.process(exampleCtClass);
    }

    @Test
    public void testGenerateValidationChecks() throws Exception {
        final String fieldName = "_value";
        final String checkType = "net.sf.oval.constraint.NotNullCheck";
        final String checkName = ValidationProcessor.getCheckName(fieldName, checkType);

        final ValidationProcessor processor = new ValidationProcessor();
        final ClassPool classPool = createClassPool();
        final CtClass ctClass = classPool.get(
                "com.arpnetworking.commons.builder.ValidationProcessorTest$ExampleBuilder");

        final StringBuilder validationChecksCode = new StringBuilder();
        final StringBuilder staticInitializerCode = new StringBuilder();
        final List<String> staticFields = Lists.newArrayList();
        processor.generateValidationChecks(
                ctClass,
                validationChecksCode,
                staticInitializerCode,
                staticFields);

        Assert.assertEquals(
                ValidationProcessor.generateValidation(
                        Mockito.mock(Annotation.class),
                        checkType,
                        checkName,
                        fieldName),
                validationChecksCode.toString());

        Assert.assertEquals(
                ValidationProcessor.generateCheckInitializer(
                        ExampleBuilder.class.getName(),
                        checkName,
                        fieldName,
                        NotNull.class.getName()),
                staticInitializerCode.toString());

        Assert.assertEquals(2, staticFields.size());
        Assert.assertEquals(
                ValidationProcessor.generateCheckFieldDeclaration(
                        checkType,
                        checkName),
                staticFields.get(0));
        Assert.assertEquals(
                ValidationProcessor.generateFieldContextDeclaration(
                        ExampleBuilder.class.getName(),
                        checkName,
                        fieldName),
                staticFields.get(1));
    }

    @Test
    public void testGenerateCheckFieldDeclaration() {
        final String fieldName = "_foo";
        final String checkType = "net.sf.oval.constraint.NotNullCheck";
        final String checkName = ValidationProcessor.getCheckName(fieldName, checkType);
        Assert.assertEquals(
                "private static final net.sf.oval.constraint.NotNullCheck "
                        + checkName
                        + " = new net.sf.oval.constraint.NotNullCheck();",
                ValidationProcessor.generateCheckFieldDeclaration(checkType, checkName));
    }

    @Test
    public void testGenerateFieldContextDeclaration() {
        final String className = "com.arpnetworking.commons.builder.ValidationProcessorTest$ExampleBuilder";
        final String fieldName = "_foo";
        final String checkType = "net.sf.oval.constraint.NotNullCheck";
        final String checkName = ValidationProcessor.getCheckName(fieldName, checkType);
        Assert.assertEquals(
                "private static final net.sf.oval.context.OValContext "
                        + checkName + "_CONTEXT = new net.sf.oval.context.FieldContext("
                        + className + ".class, \"_foo\");",
                ValidationProcessor.generateFieldContextDeclaration(
                        className,
                        checkName,
                        fieldName));
    }

    @Test
    public void testGenerateCheckInitializer() {
        final String fieldName = "_foo";
        final String checkType = "net.sf.oval.constraint.NotNullCheck";
        final String checkName = ValidationProcessor.getCheckName(fieldName, checkType);
        Assert.assertEquals(
                checkName + ".configure(\n"
                        + "com.arpnetworking.commons.builder.ValidationProcessorTest$ImmediateBuilder.class"
                        + ".getDeclaredField(\"_foo\")\n"
                        + ".getDeclaredAnnotation(net.sf.oval.constraint.NotNull.class));\n",
                ValidationProcessor.generateCheckInitializer(
                        ImmediateBuilder.class.getName(),
                        checkName,
                        fieldName,
                        NotNull.class.getName()));
    }

    @Test
    public void testGenerateValidation() {
        final String fieldName = "_foo";
        final String checkType = "net.sf.oval.constraint.NotNullCheck";
        final String checkName = ValidationProcessor.getCheckName(fieldName, checkType);
        Assert.assertEquals(
                "if (!" + checkName + ".isSatisfied(this, _foo, null, null)) {\n"
                        + "violations.add(new net.sf.oval.ConstraintViolation("
                        + checkName + ", " + checkName + ".getMessage(), this, _foo, " + checkName + "_CONTEXT));\n"
                        + "}\n",
                ValidationProcessor.generateValidation(
                        Mockito.mock(Annotation.class),
                        checkType,
                        checkName,
                        fieldName));
    }

    @Test
    public void testGenerateValidationWithMethod() {
        final ValidateWithMethod annotation = Mockito.mock(ValidateWithMethod.class);
        Mockito.doReturn("checkMe").when(annotation).methodName();

        final String fieldName = "_foo";
        final String checkType = "net.sf.oval.constraint.ValidateWithMethodCheck";
        final String checkName = ValidationProcessor.getCheckName(fieldName, checkType);
        Assert.assertEquals(
                "if (" + fieldName + " == null && " + checkName + ".isIgnoreIfNull()) {\n"
                        + "return true;\n"
                        + "}\n"
                        + "if (!checkMe(_foo)) {\n"
                        + "violations.add(new net.sf.oval.ConstraintViolation("
                        + checkName + ", " + checkName + ".getMessage(), this, _foo, " + checkName + "_CONTEXT));\n"
                        + "}\n",
                ValidationProcessor.generateValidation(
                        annotation,
                        checkType,
                        checkName,
                        fieldName));
    }

    @Test
    public void testGetCheckName() {
        Assert.assertEquals(
                "_FOO_NET_SF_OVAL_CONSTRAINT_NOTNULLCHECK",
                ValidationProcessor.getCheckName("_foo", "net.sf.oval.constraint.NotNullCheck"));
    }

    private static ClassPool createClassPool() {
        final ClassPool classPool = new ClassPool(ClassPool.getDefault());
        classPool.appendClassPath(
                new LoaderClassPath(Thread.currentThread()
                        .getContextClassLoader()));
        return classPool;
    }

    private static void assertViolation(
            final ConstraintsViolatedException e,
            final Class<?> checkClass,
            final Object invalidValue,
            final String fieldName,
            final Object validatedObject) {
        final ConstraintViolation[] violations = e.getConstraintViolations();
        Assert.assertEquals("Violations: " + Arrays.toString(violations), 1, violations.length);
        Assert.assertEquals(checkClass.getName(), violations[0].getCheckName());
        Assert.assertEquals(invalidValue, violations[0].getInvalidValue());
        Assert.assertTrue(violations[0].getContext() instanceof FieldContext);
        final FieldContext context = (FieldContext) violations[0].getContext();
        Assert.assertEquals(fieldName, context.getField().getName());
        Assert.assertSame(validatedObject, violations[0].getValidatedObject());
    }

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    private static void writeClassFile(final CtClass ctClass, final String fileName) {
        final Path path = Paths.get("./target/test-data");
        path.toFile().mkdirs();
        final Path file = path.resolve(fileName);
        try (final DataOutputStream outputStream = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(file.toString())))) {
            ctClass.toBytecode(outputStream);
        } catch (final IOException | CannotCompileException e) {
            throw Throwables.propagate(e);
        }
    }

    @SkipValidationProcessor
    private static final class SkippedBuilder extends OvalBuilder {

        protected SkippedBuilder(final Function targetConstructor) {
            super(targetConstructor);
        }
    }

    private abstract static class ImmediateBuilder extends OvalBuilder {

        protected ImmediateBuilder(final Function targetConstructor) {
            super(targetConstructor);
        }
    }


    private static final class DescendentBuilder extends ImmediateBuilder {

        protected DescendentBuilder(final Function targetConstructor) {
            super(targetConstructor);
        }
    }

    @SkipValidationProcessor
    private static class ExampleBuilder extends OvalBuilder {

        ExampleBuilder(final Function targetConstructor) {
            super(targetConstructor);
        }

        public ExampleBuilder setValue(final Object value) {
            _value = value;
            return this;
        }

        @NotNull
        private Object _value;
    }

    @SkipValidationProcessor
    private abstract static class FrozenBuilder extends OvalBuilder {

        protected FrozenBuilder(final Function targetConstructor) {
            super(targetConstructor);
        }
    }

    /**
     * There are two strange things going on here.
     *
     * 1) The builders are not nested inside pojo classes. When this is done
     * and the builder is renamed after processing, which happens only in these
     * tests, it breaks the the static parent class link.
     *
     * See:
     * https://issues.jboss.org/browse/JASSIST-136
     *
     * Consequently, the builders are all nested direclty under the test to
     * which they do not use their parent reference.
     *
     * 2) There is a single pojo class for simplicity. However, it must use
     * reflection to retrieve the value since the builder class is one of
     * six; three concrete builders and a second processed variant of each.
     */
    private static class ExamplePojo {

        ExamplePojo(final Builder builder) {
            try {
                _value = builder.getClass().getMethod("getValue").invoke(builder);
                // CHECKSTYLE.OFF: IllegalCatch - Constructor is not allowed to throw.
            } catch (final Exception e) {
                // CHECKSTYLE.ON: IllegalCatch
                throw Throwables.propagate(e);
            }
        }

        public Object getValue() {
            return _value;
        }

        private final Object _value;
    }

    @SkipValidationProcessor
    private static class ComparisonBuilder extends NonValidatingBaseBuilder<ExamplePojo> {

        ComparisonBuilder() {
            super(ExamplePojo::new);
        }

        public ComparisonBuilder setValue(final Object value) {
            _value = value;
            return this;
        }

        public Object getValue() {
            return _value;
        }

        @NotNull
        private Object _value;
    }

    @SkipValidationProcessor
    private static class NoChangeBuilder extends OvalBuilder<ExamplePojo> {

        NoChangeBuilder() {
            super(ExamplePojo::new);
        }

        public NoChangeBuilder setValue(final Object value) {
            _value = value;
            return this;
        }

        public Object getValue() {
            return _value;
        }

        @NotNull
        private Object _value;
    }

    @SkipValidationProcessor
    private static class NoConstraintsBuilder extends OvalBuilder<ExamplePojo> {

        @SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
        NoConstraintsBuilder() {
            super(ExamplePojo::new);
        }

        public NoConstraintsBuilder setValue(final Object value) {
            _value = value;
            return this;
        }

        public Object getValue() {
            return _value;
        }

        @Nonnull // This annotation is ignored
        private Object _value;
    }

    @SkipValidationProcessor
    private abstract static class ParentBuilder<B> extends OvalBuilder<ExamplePojo> {

        @SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
        ParentBuilder() {
            super(ExamplePojo::new);
        }

        public B setValue(final Object value) {
            _value = value;
            return self();
        }

        public Object getValue() {
            return _value;
        }

        protected abstract B self();

        @NotNull
        private Object _value;
    }

    @SkipValidationProcessor
    private static class UnprocessedParentBuilder extends ParentBuilder<UnprocessedParentBuilder> {

        @SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
        UnprocessedParentBuilder() {
            super();
        }

        public UnprocessedParentBuilder setOtherValue(final Object value) {
            _otherValue = value;
            return self();
        }

        public Object getOtherValue() {
            return _otherValue;
        }

        @Override
        protected UnprocessedParentBuilder self() {
            return this;
        }

        @NotNull
        private Object _otherValue;
    }
}
