/*
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

import com.arpnetworking.commons.builder.annotations.WovenValidation;
import com.arpnetworking.commons.builder.processorbuilder.ComparisonBuilder;
import com.arpnetworking.commons.builder.processorbuilder.ExampleBuilder;
import com.arpnetworking.commons.builder.processorbuilder.ExamplePojo;
import com.arpnetworking.commons.builder.processorbuilder.ImmediateBuilder;
import com.arpnetworking.commons.builder.processorbuilder.NoChangeBuilder;
import com.arpnetworking.commons.builder.processorbuilder.NoConstraintsBuilder;
import com.arpnetworking.commons.builder.processorbuilder.UnprocessedParentBuilder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import net.sf.oval.ConstraintViolation;
import net.sf.oval.constraint.EqualToField;
import net.sf.oval.constraint.NotEqualToField;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Tests for the {@link ValidationProcessor}.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
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
                "com.arpnetworking.commons.builder.processorbuilder.SkippedBuilder")));
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
                "com.arpnetworking.commons.builder.processorbuilder.ImmediateBuilder")));
        Assert.assertTrue(processor.accept(classPool.get(
                "com.arpnetworking.commons.builder.processorbuilder.DescendentBuilder")));
    }

    @Test
    public void testMarkClassAsWeaved() throws Exception {
        final ValidationProcessor processor = new ValidationProcessor();
        final ClassPool classPool = createClassPool();

        // Process a simple class
        // NOTE: we run this on a non-builder class because we need a class with no annotations to get
        // full code coverage. The builder classes are processed as part of the build and therefore
        // get an annotation applied to them (or are skipped by having an annotation applied to them)
        final CtClass exampleCtClass = classPool.get(
                "com.arpnetworking.commons.builder.processorbuilder.SimpleClass");

        exampleCtClass.defrost();
        processor.markAsProcessed(exampleCtClass);
        exampleCtClass.defrost();
        exampleCtClass.setName("com.arpnetworking.commons.builder.processorbuilder.ProcessedSimpleClass");
        exampleCtClass.getClassFile().compact();
        exampleCtClass.rebuildClassFile();

        // Output the class file for debugging
        writeClassFile(exampleCtClass, "ValidationProcessorTest.ProcessedSimpleClass.class");

        Assert.assertNotNull(exampleCtClass.getAnnotation(WovenValidation.class));

        // Make sure we can re-process the class and not blow up adding the annotation a second time
        exampleCtClass.defrost();
        processor.markAsProcessed(exampleCtClass);
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
                "com.arpnetworking.commons.builder.processorbuilder.ComparisonBuilder");

        exampleCtClass.defrost();
        processor.process(exampleCtClass);
        exampleCtClass.defrost();
        exampleCtClass.setName("com.arpnetworking.commons.builder.processorbuilder.ProcessedComparisonBuilder");
        exampleCtClass.getClassFile().compact();
        exampleCtClass.rebuildClassFile();

        // Output the class file for debugging
        writeClassFile(exampleCtClass, "ValidationProcessorTest.ProcessedComparisonBuilder.class");

        // Assert that validation happens after processing
        @SuppressWarnings("unchecked")
        final OvalBuilder<ExamplePojo> processedBuilder = (OvalBuilder<ExamplePojo>) exampleCtClass.toClass(ComparisonBuilder.class)
                .getDeclaredConstructor()
                .newInstance();
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
                "com.arpnetworking.commons.builder.processorbuilder.NoChangeBuilder");

        exampleCtClass.defrost();
        processor.process(exampleCtClass);
        exampleCtClass.defrost();
        exampleCtClass.setName("com.arpnetworking.commons.builder.processorbuilder.ProcessedNoChangeBuilder");
        exampleCtClass.getClassFile().compact();
        exampleCtClass.rebuildClassFile();

        // Output the class file for debugging
        writeClassFile(exampleCtClass, "ValidationProcessorTest.ProcessedNoChangeBuilder.class");

        // Assert that validation _still_ happens after processing
        @SuppressWarnings("unchecked")
        final OvalBuilder<ExamplePojo> processedBuilder = (OvalBuilder<ExamplePojo>) exampleCtClass.toClass(NoChangeBuilder.class)
                .getDeclaredConstructor()
                .newInstance();
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
                "com.arpnetworking.commons.builder.processorbuilder.NoConstraintsBuilder");

        exampleCtClass.defrost();
        processor.process(exampleCtClass);
        exampleCtClass.defrost();
        exampleCtClass.setName("com.arpnetworking.commons.builder.processorbuilder.ProcesNoConstraintsBuilder");
        exampleCtClass.getClassFile().compact();
        exampleCtClass.rebuildClassFile();

        // Output the class file for debugging
        writeClassFile(exampleCtClass, "ValidationProcessorTest.ProcessedNoConstraintsBuilder.class");

        // Assert that no validation happens after processing
        @SuppressWarnings("unchecked")
        final OvalBuilder<ExamplePojo> builder = (OvalBuilder<ExamplePojo>) exampleCtClass.toClass(NoConstraintsBuilder.class)
                .getDeclaredConstructor()
                .newInstance();
        final ExamplePojo postProcessedPojo = builder.build();
        Assert.assertNull(postProcessedPojo.getValue());
    }

    @Test
    @SuppressFBWarnings("NP_NULL_PARAM_DEREF_ALL_TARGETS_DANGEROUS")
    public void testUnprocessedParentPojo() throws Exception {
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
                "com.arpnetworking.commons.builder.processorbuilder.UnprocessedParentBuilder");

        exampleCtClass.defrost();
        processor.process(exampleCtClass);
        exampleCtClass.defrost();
        exampleCtClass.setName("com.arpnetworking.commons.builder.processorbuilder.ProcesUnprocessedParentBuilder");
        exampleCtClass.getClassFile().compact();
        exampleCtClass.rebuildClassFile();

        // Output the class file for debugging
        writeClassFile(exampleCtClass, "ValidationProcessorTest.ProcessedUnprocessedParentBuilder.class");

        // Assert that validation happens after processing
        @SuppressWarnings("unchecked")
        final OvalBuilder<ExamplePojo> builder = (OvalBuilder<ExamplePojo>) exampleCtClass.toClass(UnprocessedParentBuilder.class)
                .getDeclaredConstructor()
                .newInstance();
        try {
            builder.build();
        } catch (final ConstraintsViolatedException e) {
            Assert.assertEquals(2, e.getConstraintViolations().length);
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
                "com.arpnetworking.commons.builder.processorbuilder.FrozenBuilder");
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
                "com.arpnetworking.commons.builder.processorbuilder.ExampleBuilder");

        final StringBuilder validationChecksCode = new StringBuilder();
        final StringBuilder staticInitializerCode = new StringBuilder();
        // CHECKSTYLE.OFF: IllegalInstantiation - No Guava
        final List<String> staticFields = new ArrayList<>();
        // CHECKSTYLE.ON: IllegalInstantiation
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
        final String className = "com.arpnetworking.commons.builder.processorbuilder.ExampleBuilder";
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
                        + "com.arpnetworking.commons.builder.processorbuilder.ImmediateBuilder.class"
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
                "final com.arpnetworking.commons.builder.OBValidationCycle cycle = "
                        + "new com.arpnetworking.commons.builder.OBValidationCycle(this);\n"
                        + "if (!" + checkName + ".isSatisfied(this, _foo, cycle)) {\n"
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
                "if (!(" + fieldName + " == null && " + checkName + ".isIgnoreIfNull()) && !checkMe(_foo)) {\n"
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
    public void testGenerateValidationWithEqualToField() {
        final EqualToField annotation = Mockito.mock(EqualToField.class);
        Mockito.doReturn("_otherField").when(annotation).value();
        Mockito.doReturn(false).when(annotation).useGetter();

        final String fieldName = "_foo";
        final String checkType = "net.sf.oval.constraint.EqualToFieldCheck";
        final String checkName = ValidationProcessor.getCheckName(fieldName, checkType);
        Assert.assertEquals(
                "if (" + fieldName + " != null && (_otherField == null || !" + fieldName + ".equals(_otherField))) {\n"
                        + "violations.add(new net.sf.oval.ConstraintViolation(_FOO_NET_SF_OVAL_CONSTRAINT_EQUALTOFIELDCHECK, "
                        + checkName + ".getMessage(), this, " + fieldName + ", " + checkName + "_CONTEXT));\n"
                        + "}\n",
                ValidationProcessor.generateValidation(
                        annotation,
                        checkType,
                        checkName,
                        fieldName));
    }

    @Test
    public void testGenerateValidationWithEqualToFieldWithGetter() {
        final EqualToField annotation = Mockito.mock(EqualToField.class);
        Mockito.doReturn("_otherField").when(annotation).value();
        Mockito.doReturn(true).when(annotation).useGetter();

        final String fieldName = "_foo";
        final String checkType = "net.sf.oval.constraint.EqualToFieldCheck";
        final String checkName = ValidationProcessor.getCheckName(fieldName, checkType);
        Assert.assertEquals(
                "final com.arpnetworking.commons.builder.OBValidationCycle cycle = "
                        + "new com.arpnetworking.commons.builder.OBValidationCycle(this);\n"
                        + "if (!" + checkName + ".isSatisfied(this, _foo, cycle)) {\n"
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
    public void testGenerateValidationWithNotEqualToField() {
        final NotEqualToField annotation = Mockito.mock(NotEqualToField.class);
        Mockito.doReturn("_otherField").when(annotation).value();
        Mockito.doReturn(false).when(annotation).useGetter();

        final String fieldName = "_foo";
        final String checkType = "net.sf.oval.constraint.NotEqualToFieldCheck";
        final String checkName = ValidationProcessor.getCheckName(fieldName, checkType);
        Assert.assertEquals(
                "if (" + fieldName + " != null && _otherField != null && " + fieldName + ".equals(_otherField)) {\n"
                        + "violations.add(new net.sf.oval.ConstraintViolation(_FOO_NET_SF_OVAL_CONSTRAINT_NOTEQUALTOFIELDCHECK, "
                        + checkName + ".getMessage(), this, " + fieldName + ", " + checkName + "_CONTEXT));\n"
                        + "}\n",
                ValidationProcessor.generateValidation(
                        annotation,
                        checkType,
                        checkName,
                        fieldName));
    }

    @Test
    public void testGenerateValidationWithNotEqualToFieldWithGetter() {
        final NotEqualToField annotation = Mockito.mock(NotEqualToField.class);
        Mockito.doReturn("_otherField").when(annotation).value();
        Mockito.doReturn(true).when(annotation).useGetter();

        final String fieldName = "_foo";
        final String checkType = "net.sf.oval.constraint.NotEqualToFieldCheck";
        final String checkName = ValidationProcessor.getCheckName(fieldName, checkType);
        Assert.assertEquals(
                "final com.arpnetworking.commons.builder.OBValidationCycle cycle = "
                        + "new com.arpnetworking.commons.builder.OBValidationCycle(this);\n"
                        + "if (!" + checkName + ".isSatisfied(this, _foo, cycle)) {\n"
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
            @Nullable final Object invalidValue,
            final String fieldName,
            final Object validatedObject) {
        final ConstraintViolation[] violations = e.getConstraintViolations();
        Assert.assertEquals("Violations: " + Arrays.toString(violations), 1, violations.length);
        Assert.assertEquals(checkClass.getName(), violations[0].getCheckName());
        Assert.assertEquals(invalidValue, violations[0].getInvalidValue());
        Assert.assertTrue(violations[0].getContextPath().get(0) instanceof FieldContext);
        final FieldContext context = (FieldContext) violations[0].getContextPath().get(0);
        Assert.assertEquals(fieldName, context.getField().getName());
        Assert.assertSame(validatedObject, violations[0].getValidatedObject());
    }

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    private static void writeClassFile(final CtClass ctClass, final String fileName) {
        final Path path = Paths.get("./target/test-data");
        path.toFile().mkdirs();
        final Path file = path.resolve(fileName);
        try (DataOutputStream outputStream = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(file.toString())))) {
            ctClass.toBytecode(outputStream);
        } catch (final IOException | CannotCompileException e) {
            throw new RuntimeException(e);
        }
    }


}
