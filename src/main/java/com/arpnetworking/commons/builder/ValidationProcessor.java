/*
 * Copyright 2016 Inscope Metrics
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
import com.arpnetworking.commons.maven.javassist.ClassProcessor;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.SyntheticAttribute;
import net.sf.oval.configuration.annotation.AnnotationCheck;
import net.sf.oval.configuration.annotation.Constraint;
import net.sf.oval.constraint.AssertFalse;
import net.sf.oval.constraint.AssertNull;
import net.sf.oval.constraint.AssertTrue;
import net.sf.oval.constraint.AssertURL;
import net.sf.oval.constraint.CheckWith;
import net.sf.oval.constraint.DateRange;
import net.sf.oval.constraint.Digits;
import net.sf.oval.constraint.Email;
import net.sf.oval.constraint.EqualToField;
import net.sf.oval.constraint.Future;
import net.sf.oval.constraint.HasSubstring;
import net.sf.oval.constraint.InstanceOf;
import net.sf.oval.constraint.InstanceOfAny;
import net.sf.oval.constraint.Length;
import net.sf.oval.constraint.MatchPattern;
import net.sf.oval.constraint.Max;
import net.sf.oval.constraint.MaxLength;
import net.sf.oval.constraint.MaxSize;
import net.sf.oval.constraint.MemberOf;
import net.sf.oval.constraint.Min;
import net.sf.oval.constraint.MinLength;
import net.sf.oval.constraint.MinSize;
import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.NotEqual;
import net.sf.oval.constraint.NotEqualToField;
import net.sf.oval.constraint.NotMatchPattern;
import net.sf.oval.constraint.NotMemberOf;
import net.sf.oval.constraint.NotNegative;
import net.sf.oval.constraint.NotNull;
import net.sf.oval.constraint.Past;
import net.sf.oval.constraint.Range;
import net.sf.oval.constraint.Size;
import net.sf.oval.constraint.ValidateWithMethod;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Implementation of {@link ClassProcessor} for adding code to {@link OvalBuilder}
 * implementations to validate constraints. This replaces the reflection based
 * validation in the OVal framework but uses the same check classes.
 *
 * Dependencies:
 * <ul>
 *     <li>com.arpnetworking.commons:javassist-maven-plugin (as plugin only)</li>
 *     <li>com.arpnetworking.commons:javassist-maven-core (as provided, only needed to avoid compilation warnings</li>
 *     <li>net.sf.oval:oval</li>
 *     <li>com.google.guava:guava</li>
 * </ul>
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public final class ValidationProcessor implements ClassProcessor {

    @Override
    public boolean accept(final CtClass ctClass) {
        // Reject processing of OvalBuilder itself
        if (OVAL_BUILDER_CLASS.equals(ctClass.getName())) {
            return false;
        }
        // Reject any classes annotated with SkipValidationProcessor
        try {
            for (final Object object : ctClass.getAnnotations()) {
                final Annotation annotation = (Annotation) object;
                if (SKIP_VALIDATION_TRANSFORM_CLASS.equals(annotation.annotationType().getName())) {
                    return false;
                }
            }
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(String.format("Unable to evaluate class %s", ctClass.getName()), e);
        }
        // Accept any classes which descend from OvalBuilder
        CtClass parent = ctClass;
        while (parent != null) {
            if (OVAL_BUILDER_CLASS.equals(parent.getName())) {
                return true;
            }
            try {
                parent = parent.getSuperclass();
            } catch (final NotFoundException e) {
                throw new RuntimeException(String.format("Unable to evaluate class %s", ctClass.getName()), e);
            }
        }
        // All other classes are rejected
        return false;
    }

    @SuppressFBWarnings("REC_CATCH_EXCEPTION")
    @Override
    public void process(final CtClass ctClass) {
        try {
            final String collectSuperClassViolations;
            if (!OVAL_BUILDER_CLASS.equals(ctClass.getSuperclass().getName())) {
                collectSuperClassViolations = "super.validate(violations);\n";
            } else {
                collectSuperClassViolations = "";
            }

            // Build code to inject
            final StringBuilder validationChecksCode = new StringBuilder();
            final StringBuilder staticInitializerCode = new StringBuilder();
            // CHECKSTYLE.OFF: IllegalInstantiation - No Guava
            final List<String> staticFields = new ArrayList<>();
            // CHECKSTYLE.ON: IllegalInstantiation
            generateValidationChecks(ctClass, validationChecksCode, staticInitializerCode, staticFields);

            // Add the static fields
            for (final String staticField : staticFields) {
                ctClass.addField(CtField.make(staticField, ctClass));
            }

            // Add validation method
            final CtMethod validationMethod = CtNewMethod.make(
                    "protected void validate(java.util.List violations) {\n"
                            + collectSuperClassViolations
                            + validationChecksCode.toString()
                            + "}",
                    ctClass);
            final SyntheticAttribute syntheticAttribute = new SyntheticAttribute(ctClass.getClassFile().getConstPool());
            validationMethod.setAttribute(syntheticAttribute.getName(), syntheticAttribute.get());
            ctClass.addMethod(validationMethod);

            // Add field-check initializers
            if (staticInitializerCode.length() > 0) {
                final CtConstructor staticInitializer = ctClass.makeClassInitializer();
                staticInitializer.insertAfter(
                        "try {\n"
                                + staticInitializerCode.toString()
                                + "} catch (NoSuchFieldException e) {\n"
                                + "throw new RuntimeException(\"Constraint check configuration error\", e);\n"
                                + "}"
                );
            }
            markAsProcessed(ctClass);

            // CHECKSTYLE.OFF: IllegalCatch
        } catch (final Exception e) {
            // CHECKSTYLE.ON: IllegalCatch
            throw new RuntimeException(e);
        }
    }

    /* package private */ void generateValidationChecks(
            final CtClass ctClass,
            final StringBuilder validationChecksCode,
            final StringBuilder staticInitializerCode,
            final List<String> staticFields) {

        for (final CtField ctField : ctClass.getDeclaredFields()) {
            for (final Object annotationObject : ctField.getAvailableAnnotations()) {
                final Annotation annotation = (Annotation) annotationObject;
                if (ANNOTATIONS.contains(annotation.annotationType())) {
                    final Constraint constraint = annotation.annotationType().getAnnotation(Constraint.class);
                    final Class<? extends AnnotationCheck<? extends Annotation>> checkClass = constraint.checkWith();
                    final String checkType = checkClass.getName();
                    final String checkName = getCheckName(ctField.getName(), checkType);
                    final String fieldName = ctField.getName();

                    // Define the member for the check
                    staticFields.add(generateCheckFieldDeclaration(checkType, checkName));

                    // Define the member for the field context
                    staticFields.add(generateFieldContextDeclaration(ctClass.getName(), checkName, fieldName));

                    // Write the check initializer code
                    staticInitializerCode.append(
                            generateCheckInitializer(
                                    ctClass.getName(),
                                    checkName,
                                    fieldName,
                                    annotation.annotationType().getName()));

                    // Write the validation code
                    validationChecksCode.append(generateValidation(
                            annotation,
                            checkType,
                            checkName,
                            fieldName));
                }
            }
        }
    }

    /* package private */ static String generateCheckFieldDeclaration(
            final String checkType,
            final String checkName) {
        return "private static final " + checkType + " " + checkName + " = new " + checkType + "();";
    }

    /* package private */ static String generateFieldContextDeclaration(
            final String className,
            final String checkName,
            final String fieldName) {
        return "private static final net.sf.oval.context.OValContext " + checkName + "_CONTEXT"
                + " = new net.sf.oval.context.FieldContext(" + className + ".class, \"" + fieldName + "\");";
    }

    /* package private */ static String generateCheckInitializer(
            final String className,
            final String checkName,
            final String fieldName,
            final String annotationTypeName) {
        return checkName + ".configure(\n"
                + className + ".class.getDeclaredField(\"" + fieldName + "\")\n"
                + ".getDeclaredAnnotation(" + annotationTypeName + ".class));\n";
    }

    /* package private */ static String generateValidation(
            final Annotation annotation,
            final String checkType,
            final String checkName,
            final String fieldName) {

        if (VALIDATE_WITH_METHOD_CHECK.equals(checkType)) {
            // Special Case: Unwrap the check method and code generate its invocation
            final ValidateWithMethod validateWithMethod = (ValidateWithMethod) annotation;
            return "if (!(" + fieldName + " == null && " + checkName + ".isIgnoreIfNull()) && !"
                    + validateWithMethod.methodName() + "(" + fieldName + ")) {\n"
                    + "violations.add(new net.sf.oval.ConstraintViolation("
                    + checkName + ", " + checkName + ".getMessage(), this, " + fieldName + ", " + checkName + "_CONTEXT));\n"
                    + "}\n";
        } else if (FIELDS_EQUAL_CHECK.equals(checkType)) {
            // Special Case: Unwrap the other field reference and code generate its comparison
            // NOTE: This does not support getter usage.
            final EqualToField equalTo = (EqualToField) annotation;
            if (!equalTo.useGetter()) {
                return "if (" + fieldName + " != null && (" + equalTo.value() + " == null "
                        + "|| !" + fieldName + ".equals(" + equalTo.value() + "))) {\n"
                        + "violations.add(new net.sf.oval.ConstraintViolation("
                        + checkName + ", " + checkName + ".getMessage(), this, " + fieldName + ", " + checkName + "_CONTEXT));\n"
                        + "}\n";
            }
        } else if (FIELDS_NOT_EQUAL_CHECK.equals(checkType)) {
            // Special Case: Unwrap the other field reference and code generate its comparison
            // NOTE: This does not support getter usage.
            final NotEqualToField notEqualTo = (NotEqualToField) annotation;
            if (!notEqualTo.useGetter()) {
                return "if (" + fieldName + " != null && " + notEqualTo.value() + " != null "
                        + "&& " + fieldName + ".equals(" + notEqualTo.value() + ")) {\n"
                        + "violations.add(new net.sf.oval.ConstraintViolation("
                        + checkName + ", " + checkName + ".getMessage(), this, " + fieldName + ", " + checkName + "_CONTEXT));\n"
                        + "}\n";
            }
        }
        return  "final " + VALIDATION_CYCLE_CLASS + " cycle = new " + VALIDATION_CYCLE_CLASS + "(this);\n"
                + "if (!" + checkName + ".isSatisfied(this, " + fieldName + ", cycle)) {\n"
                + "violations.add(new net.sf.oval.ConstraintViolation("
                + checkName + ", " + checkName + ".getMessage(), this, " + fieldName + ", " + checkName + "_CONTEXT));\n"
                + "}\n";
    }

    // NOTE: Package private for testing
    /* package private */ static String getCheckName(
            final String fieldName,
            final String checkName) {
        // I was going to try and generate "nice" private static final member
        // names from the field and check names but this would only work if the
        // user employed a set of naming conventions.
        //
        // Otherwise, it is not possible to ensure that check names do not
        // collide. So we're going to use a very naive approach to avoid any
        // collisions.
        return (fieldName + "_" + checkName).toUpperCase(Locale.getDefault()).replace('.', '_');
    }

    /* package private */ void markAsProcessed(final CtClass ctClass) {
        final ClassFile classFile = ctClass.getClassFile();
        for (final Object attributeObject : classFile.getAttributes()) {
            if (attributeObject instanceof AnnotationsAttribute) {
                final AnnotationsAttribute annotationAttribute = (AnnotationsAttribute) attributeObject;
                final javassist.bytecode.annotation.Annotation annotation = annotationAttribute.getAnnotation(PROCESSED_ANNOTATION_CLASS);
                if (annotation != null) {
                    return;
                }
            }
        }

        final javassist.bytecode.annotation.Annotation annotation =
                new javassist.bytecode.annotation.Annotation(PROCESSED_ANNOTATION_CLASS, classFile.getConstPool());
        final AnnotationsAttribute annotationAttribute =
                new AnnotationsAttribute(classFile.getConstPool(), AnnotationsAttribute.visibleTag);
        annotationAttribute.addAnnotation(annotation);
        classFile.addAttribute(annotationAttribute);
    }


    // CHECKSTYLE.OFF: IllegalInstantiation - No Guava
    private static final Set<Class<?>> ANNOTATIONS = new HashSet<>();
    // CHECKSTYLE.ON: IllegalInstantiation
    private static final String OVAL_BUILDER_CLASS =
            "com.arpnetworking.commons.builder.OvalBuilder";
    private static final String VALIDATION_CYCLE_CLASS =
            "com.arpnetworking.commons.builder.OBValidationCycle";
    private static final String SKIP_VALIDATION_TRANSFORM_CLASS =
            "com.arpnetworking.commons.builder.annotations.SkipValidationProcessor";
    private static final String VALIDATE_WITH_METHOD_CHECK =
            "net.sf.oval.constraint.ValidateWithMethodCheck";
    private static final String FIELDS_EQUAL_CHECK =
            "net.sf.oval.constraint.EqualToFieldCheck";
    private static final String FIELDS_NOT_EQUAL_CHECK =
            "net.sf.oval.constraint.NotEqualToFieldCheck";
    private static final String PROCESSED_ANNOTATION_CLASS = WovenValidation.class.getCanonicalName();

    // CHECKSTYLE.OFF: ExecutableStatementCount - Initialization
    static {
        ANNOTATIONS.add(AssertFalse.class);
        ANNOTATIONS.add(AssertNull.class);
        ANNOTATIONS.add(AssertTrue.class);
        ANNOTATIONS.add(AssertURL.class);
        ANNOTATIONS.add(CheckWith.class);
        ANNOTATIONS.add(DateRange.class);
        ANNOTATIONS.add(Digits.class);
        ANNOTATIONS.add(Email.class);
        ANNOTATIONS.add(EqualToField.class);
        ANNOTATIONS.add(Future.class);
        ANNOTATIONS.add(HasSubstring.class);
        ANNOTATIONS.add(InstanceOfAny.class);
        ANNOTATIONS.add(InstanceOf.class);
        ANNOTATIONS.add(Length.class);
        ANNOTATIONS.add(MatchPattern.class);
        ANNOTATIONS.add(Max.class);
        ANNOTATIONS.add(MaxLength.class);
        ANNOTATIONS.add(MaxSize.class);
        ANNOTATIONS.add(MemberOf.class);
        ANNOTATIONS.add(Min.class);
        ANNOTATIONS.add(MinLength.class);
        ANNOTATIONS.add(MinSize.class);
        ANNOTATIONS.add(NotBlank.class);
        ANNOTATIONS.add(NotEmpty.class);
        ANNOTATIONS.add(NotEqual.class);
        ANNOTATIONS.add(NotEqualToField.class);
        ANNOTATIONS.add(NotMatchPattern.class);
        ANNOTATIONS.add(NotMemberOf.class);
        ANNOTATIONS.add(NotNegative.class);
        ANNOTATIONS.add(NotNull.class);
        ANNOTATIONS.add(Past.class);
        ANNOTATIONS.add(Range.class);
        ANNOTATIONS.add(Size.class);
        ANNOTATIONS.add(ValidateWithMethod.class);
    }
    // CHECKSTYLE.ON: ExecutableStatementCount
}
