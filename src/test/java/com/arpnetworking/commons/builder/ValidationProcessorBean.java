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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.sf.oval.Validator;
import net.sf.oval.constraint.AssertFalse;
import net.sf.oval.constraint.AssertNull;
import net.sf.oval.constraint.AssertTrue;
import net.sf.oval.constraint.AssertURL;
import net.sf.oval.constraint.CheckWith;
import net.sf.oval.constraint.CheckWithCheck;
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
import net.sf.oval.context.OValContext;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

/**
 * Test bean pojo for validation processor.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
@SuppressFBWarnings("EI_EXPOSE_REP")
public final class ValidationProcessorBean {

    public Boolean getFalseBoolean() {
        return _falseBoolean;
    }

    public Object getNullObject() {
        return _nullObject;
    }

    public Boolean getTrueBoolean() {
        return _trueBoolean;
    }

    public String getUrlString() {
        return _urlString;
    }

    public String getCheckWithString() {
        return _checkWithString;
    }

    public Date getRangeDate() {
        return _rangeDate;
    }

    public String getDigitsString() {
        return _digitsString;
    }

    public String getEmailString() {
        return _emailString;
    }

    public String getEqualToFieldString() {
        return _equalToFieldString;
    }

    public String getEqualToFieldCounterpartString() {
        return _equalToFieldCounterpartString;
    }

    public Date getFutureDate() {
        return _futureDate;
    }

    public String getHasSubstringString() {
        return _hasSubstringString;
    }

    public Object getInstanceOfAnyObject() {
        return _instanceOfAnyObject;
    }

    public Object getInstanceOfObject() {
        return _instanceOfObject;
    }

    public String getLengthString() {
        return _lengthString;
    }

    public String getMatchPatternString() {
        return _matchPatternString;
    }

    public Integer getMaxInteger() {
        return _maxInteger;
    }

    public String getMaxLengthString() {
        return _maxLengthString;
    }

    public List<String> getMaxSizeList() {
        return _maxSizeList;
    }

    public String getMemberOfString() {
        return _memberOfString;
    }

    public Integer getMinInteger() {
        return _minInteger;
    }

    public String getMinLengthString() {
        return _minLengthString;
    }

    public List<String> getMinSizeList() {
        return _minSizeList;
    }

    public String getNotBlankString() {
        return _notBlankString;
    }

    public String getNotEmptyString() {
        return _notEmptyString;
    }

    public String getNotEqualString() {
        return _notEqualString;
    }

    public String getNotEqualToFieldString() {
        return _notEqualToFieldString;
    }

    public String getNotEqualToFieldCounterpartString() {
        return _notEqualToFieldCounterpartString;
    }

    public String getNotMatchPatternString() {
        return _notMatchPatternString;
    }

    public String getNotMemberOfString() {
        return _notMemberOfString;
    }

    public Integer getNotNegativeInteger() {
        return _notNegativeInteger;
    }

    public Object getNotNullObject() {
        return _notNullObject;
    }

    public Date getPastDate() {
        return _pastDate;
    }

    public Double getRangeDouble() {
        return _rangeDouble;
    }

    public List<String> getSizeList() {
        return _sizeList;
    }

    public String getValidateWithMethodString() {
        return _validateWithMethodString;
    }

    // CHECKSTYLE.OFF: ExecutableStatementCount - Construction
    private ValidationProcessorBean(final Builder builder) {
        _falseBoolean = builder._falseBoolean;
        _nullObject = builder._nullObject;
        _trueBoolean = builder._trueBoolean;
        _urlString = builder._urlString;
        _checkWithString = builder._checkWithString;
        _rangeDate = builder._rangeDate;
        _digitsString = builder._digitsString;
        _emailString = builder._emailString;
        _equalToFieldString = builder._equalToFieldString;
        _equalToFieldCounterpartString = builder._equalToFieldCounterpartString;
        _futureDate = builder._futureDate;
        _hasSubstringString = builder._hasSubstringString;
        _instanceOfAnyObject = builder._instanceOfAnyObject;
        _instanceOfObject = builder._instanceOfObject;
        _matchPatternString = builder._matchPatternString;
        _lengthString = builder._lengthString;
        _maxInteger = builder._maxInteger;
        _maxLengthString = builder._maxLengthString;
        _maxSizeList = builder._maxSizeList;
        _memberOfString = builder._memberOfString;
        _minInteger = builder._minInteger;
        _minLengthString = builder._minLengthString;
        _minSizeList = builder._minSizeList;
        _notBlankString = builder._notBlankString;
        _notEmptyString = builder._notEmptyString;
        _notEqualString = builder._notEqualString;
        _notEqualToFieldString = builder._notEqualToFieldString;
        _notEqualToFieldCounterpartString = builder._notEqualToFieldCounterpartString;
        _notMatchPatternString = builder._notMatchPatternString;
        _notMemberOfString = builder._notMemberOfString;
        _notNegativeInteger = builder._notNegativeInteger;
        _notNullObject = builder._notNullObject;
        _pastDate = builder._pastDate;
        _rangeDouble = builder._rangeDouble;
        _sizeList = builder._sizeList;
        _validateWithMethodString = builder._validateWithMethodString;
    }
    // CHECKSTYLE.ON: ExecutableStatementCount

    private final Boolean _falseBoolean;
    private final Object _nullObject;
    private final Boolean _trueBoolean;
    private final String _urlString;
    private final String _checkWithString;
    private final Date _rangeDate;
    private final String _digitsString;
    private final String _emailString;
    private final String _equalToFieldString;
    private final String _equalToFieldCounterpartString;
    private final Date _futureDate;
    private final String _hasSubstringString;
    private final Object _instanceOfAnyObject;
    private final Object _instanceOfObject;
    private final String _lengthString;
    private final String _matchPatternString;
    private final Integer _maxInteger;
    private final String _maxLengthString;
    private final List<String> _maxSizeList;
    private final String _memberOfString;
    private final Integer _minInteger;
    private final String _minLengthString;
    private final List<String> _minSizeList;
    private final String _notBlankString;
    private final String _notEmptyString;
    private final String _notEqualString;
    private final String _notEqualToFieldString;
    private final String _notEqualToFieldCounterpartString;
    private final String _notMatchPatternString;
    private final String _notMemberOfString;
    private final Integer _notNegativeInteger;
    private final Object _notNullObject;
    private final Date _pastDate;
    private final Double _rangeDouble;
    private final List<String> _sizeList;
    private final String _validateWithMethodString;

    /**
     * OvalBuilder implementation for ValidationProcessorBean.
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public static final class Builder extends OvalBuilder<ValidationProcessorBean> {

        /**
         * Create an instance of this builder with all fields set to valid values.
         *
         * @return New instance of this builder with all fields set to valid values.
         */
        public static Builder createValid() {
            return new ValidationProcessorBean.Builder()
                    .setFalseBoolean(false)
                    .setFutureDate(new Date(System.currentTimeMillis() + 600000))
                    .setNullObject(null)
                    .setTrueBoolean(true)
                    .setUrlString("http://localhost:80")
                    .setCheckWithString("True")
                    .setRangeDate(new Date(System.currentTimeMillis() - 600000))
                    .setDigitsString("12.0")
                    .setEmailString("me@example.com")
                    .setEqualToFieldString("Foo")
                    .setHasSubstringString("Fubar")
                    .setInstanceOfAnyObject(Optional.empty())
                    .setInstanceOfObject(UUID.randomUUID())
                    .setLengthString("ABC")
                    .setMatchPatternString("Fooo")
                    .setMaxInteger(-1)
                    .setMaxLengthString("ABC")
                    .setMaxSizeList(Arrays.asList("A", "B"))
                    .setMemberOfString("Foo")
                    .setMinInteger(1)
                    .setMinLengthString("ABC")
                    .setMinSizeList(Arrays.asList("A", "B"))
                    .setNotBlankString("NotBlank")
                    .setNotEmptyString("Foo")
                    .setNotEqualString("Bar")
                    .setNotEqualToFieldString("Bar")
                    .setNotMatchPatternString("Far")
                    .setNotMemberOfString("Cat")
                    .setNotNegativeInteger(1)
                    .setNotNullObject(LocalDateTime.now())
                    .setPastDate(new Date(System.currentTimeMillis() - 600000))
                    .setRangeDouble(1.5d)
                    .setSizeList(Arrays.asList("A", "B"))
                    .setValidateWithMethodString("true");
        }

        /**
         * Set the false boolean field.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setFalseBoolean(final Boolean value) {
            _falseBoolean = value;
            return this;
        }

        /**
         * Set the null object field.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setNullObject(@Nullable final Object value) {
            _nullObject = value;
            return this;
        }

        /**
         * Set the true boolean field.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setTrueBoolean(final Boolean value) {
            _trueBoolean = value;
            return this;
        }

        /**
         * Set the url string field.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setUrlString(final String value) {
            _urlString = value;
            return this;
        }

        /**
         * Set the check with string field.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setCheckWithString(final String value) {
            _checkWithString = value;
            return this;
        }

        /**
         * Set the range date field.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setRangeDate(final Date value) {
            _rangeDate = value;
            return this;
        }

        /**
         * Set the digit string field.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setDigitsString(final String value) {
            _digitsString = value;
            return this;
        }

        /**
         * Set the email string field.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setEmailString(final String value) {
            _emailString = value;
            return this;
        }

        /**
         * Set the equal to field string field.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setEqualToFieldString(@Nullable final String value) {
            _equalToFieldString = value;
            return this;
        }

        /**
         * Set the equal to field counterpart string field.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setEqualToFieldCounterpartString(@Nullable final String value) {
            _equalToFieldCounterpartString = value;
            return this;
        }

        /**
         * Set the future date field.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setFutureDate(final Date value) {
            _futureDate = value;
            return this;
        }

        /**
         * Set the has substring string field.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setHasSubstringString(final String value) {
            _hasSubstringString = value;
            return this;
        }

        /**
         * Set the instance of any object field.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setInstanceOfAnyObject(final Object value) {
            _instanceOfAnyObject = value;
            return this;
        }

        /**
         * Set the instance of object field.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setInstanceOfObject(final Object value) {
            _instanceOfObject = value;
            return this;
        }

        /**
         * Set the length string field.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setLengthString(final String value) {
            _lengthString = value;
            return this;
        }

        /**
         * Set the match pattern string field.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setMatchPatternString(final String value) {
            _matchPatternString = value;
            return this;
        }

        /**
         * Set the max integer field.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setMaxInteger(final Integer value) {
            _maxInteger = value;
            return this;
        }

        /**
         * Set the max length string field.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setMaxLengthString(final String value) {
            _maxLengthString = value;
            return this;
        }

        /**
         * Set the max size list field.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setMaxSizeList(final List<String> value) {
            _maxSizeList = value;
            return this;
        }

        /**
         * Set the member of string field.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setMemberOfString(final String value) {
            _memberOfString = value;
            return this;
        }

        /**
         * Set the min integer field.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setMinInteger(final Integer value) {
            _minInteger = value;
            return this;
        }

        /**
         * Set the min length string field.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setMinLengthString(final String value) {
            _minLengthString = value;
            return this;
        }

        /**
         * Set the min size list field.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setMinSizeList(final List<String> value) {
            _minSizeList = value;
            return this;
        }

        /**
         * Set the not blank string field.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setNotBlankString(final String value) {
            _notBlankString = value;
            return this;
        }

        /**
         * Set the not empty string field.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setNotEmptyString(final String value) {
            _notEmptyString = value;
            return this;
        }

        /**
         * Set the not equal string field.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setNotEqualString(final String value) {
            _notEqualString = value;
            return this;
        }

        /**
         * Set the not equal to field string field.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setNotEqualToFieldString(@Nullable final String value) {
            _notEqualToFieldString = value;
            return this;
        }

        /**
         * Set the not equal to field counterpart string field.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setNotEqualToFieldCounterpartString(@Nullable final String value) {
            _notEqualToFieldCounterpartString = value;
            return this;
        }

        /**
         * Set the not match pattern string field.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setNotMatchPatternString(final String value) {
            _notMatchPatternString = value;
            return this;
        }

        /**
         * Set the not member of string field.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setNotMemberOfString(final String value) {
            _notMemberOfString = value;
            return this;
        }

        /**
         * Set the not negative integer field.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setNotNegativeInteger(final Integer value) {
            _notNegativeInteger = value;
            return this;
        }

        /**
         * Set the not null object field.
         *
         * NOTE: Marked as nullable to faciliate null testing; normally
         * setters for fields marked @NonNull should not be marked
         * @Nullable.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setNotNullObject(@Nullable final Object value) {
            _notNullObject = value;
            return this;
        }

        /**
         * Set the past date field.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setPastDate(final Date value) {
            _pastDate = value;
            return this;
        }

        /**
         * Set the range double field.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setRangeDouble(final Double value) {
            _rangeDouble = value;
            return this;
        }

        /**
         * Set the size list field.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setSizeList(final List<String> value) {
            _sizeList = value;
            return this;
        }

        /**
         * Set the validate with method string field.
         *
         * @param value the value
         * @return this {@link Builder}
         */
        public Builder setValidateWithMethodString(@Nullable final String value) {
            _validateWithMethodString = value;
            return this;
        }

        /**
         * Public constructor.
         */
        public Builder() {
            super(ValidationProcessorBean::new);
        }

        @Override
        protected boolean isSelfValidating(final Class<? extends OvalBuilder<?>> builderClass) {
            return true;
        }

        private boolean validateWithMethod(final String value) {
            return Boolean.parseBoolean(value);
        }

        //AssertCheck
        //AssertConstraintSetCheck

        @AssertFalse
        private Boolean _falseBoolean;

        //AssertFieldConstraintsCheck

        @AssertNull
        private Object _nullObject;
        @AssertTrue
        private Boolean _trueBoolean;
        @AssertURL
        private String _urlString;

        //AssertValidCheck

        @CheckWith(value = CheckWithString.class)
        private String _checkWithString;

        //ConstraintsCheck

        @DateRange(min = "01/01/1970", max = "now", format = "dd/MM/yyyy")
        private Date _rangeDate;
        @Digits(minInteger = 1, maxInteger = 3, minFraction = 1, maxFraction = 1)
        private String _digitsString;
        @Email
        private String _emailString;
        @EqualToField("_equalToFieldCounterpartString")
        private String _equalToFieldString;
        private String _equalToFieldCounterpartString = "Foo";
        @Future
        private Date _futureDate;
        @HasSubstring("uba")
        private String _hasSubstringString;
        @InstanceOfAny(value = { java.util.Optional.class, com.google.common.base.Optional.class })
        private Object _instanceOfAnyObject;
        @InstanceOf(UUID.class)
        private Object _instanceOfObject;
        @Length(min = 2, max = 3)
        private String _lengthString;
        @MatchPattern(pattern = "Fo*")
        private String _matchPatternString;
        @Max(value = 0)
        private Integer _maxInteger;
        @MaxLength(3)
        private String _maxLengthString;
        @MaxSize(2)
        private List<String> _maxSizeList;
        @MemberOf(value = { "Foo", "Bar" })
        private String _memberOfString;
        @Min(value = 0)
        private Integer _minInteger;
        @MinLength(3)
        private String _minLengthString;
        @MinSize(2)
        private List<String> _minSizeList;

        //NoSelfReferenceCheck

        @NotBlank
        private String _notBlankString;
        @NotEmpty
        private String _notEmptyString;
        @NotEqual(value = "Foo")
        private String _notEqualString;
        @NotEqualToField("_notEqualToFieldCounterpartString")
        private String _notEqualToFieldString;
        private String _notEqualToFieldCounterpartString = "Foo";
        @NotMatchPattern(pattern = "Fo*")
        private String _notMatchPatternString;
        @NotMemberOf(value = { "Foo", "Bar" })
        private String _notMemberOfString;
        @NotNegative
        private Integer _notNegativeInteger;
        @NotNull
        private Object _notNullObject;
        @Past
        private Date _pastDate;
        @Range(min = 1.0, max = 2.0)
        private Double _rangeDouble;
        @Size(min = 1, max = 2)
        private List<String> _sizeList;
        @ValidateWithMethod(methodName = "validateWithMethod", parameterType = String.class)
        private String _validateWithMethodString;

        private static final class CheckWithString implements CheckWithCheck.SimpleCheck {

            @Override
            public boolean isSatisfied(
                    final Object validatedObject,
                    final Object value,
                    final OValContext context,
                    final Validator validator) {
                return value instanceof String && Boolean.parseBoolean((String) value);
            }

            private static final long serialVersionUID = 2379013880358366377L;
        }
    }
}
