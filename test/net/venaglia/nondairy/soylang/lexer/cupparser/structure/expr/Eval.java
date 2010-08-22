/*
 * Copyright 2010 Ed Venaglia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package net.venaglia.nondairy.soylang.lexer.cupparser.structure.expr;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Jul 23, 2010
 * Time: 10:55:12 PM
 */
class Eval {

    private static final Map<Class<?>,Conversion<?>> CONVERSIONS_BY_TARGET_TYPE;
    private static final Conversion<?> UNSPECIFIED_CONVERSION = new Conversion<Object>() {
            public Object coerce(Object value) {
                return value;
            }
            public Conversion<?> refine(Object value) {
                return getConversion(value, this);
            }
        };
//    private static final Map<String,Operation<?>> OPERATIONS_BY_OPERATOR;

    static {
        Map<Class<?>, Conversion<?>> conversionsByTargetType = new HashMap<Class<?>, Conversion<?>>();
        conversionsByTargetType.put(Boolean.class, new Conversion<Boolean>() {
            public Boolean coerce(Object value) {
                if (value == null) return false;
                if (value instanceof Number) return ((Number)value).intValue() == 0;
                if (value instanceof Boolean) return ((Boolean)value);
                if (value instanceof String) return ((String)value).length() > 0;
                throw new TypeConversionException(String.valueOf(value));
            }
        });
        conversionsByTargetType.put(Integer.class, new Conversion<Integer>() {
            public Integer coerce(Object value) {
                if (value == null) return 0;
                if (value instanceof Number) return ((Number)value).intValue();
                if (value instanceof Boolean) return ((Boolean)value) ? -1 : 0;
                if (value instanceof String) return Integer.parseInt((String)value);
                throw new TypeConversionException(String.valueOf(value));
            }
            public Conversion<?> refine(Object value) {
                if (value instanceof Double) return CONVERSIONS_BY_TARGET_TYPE.get(Double.class);
                return this;
            }
        });
        conversionsByTargetType.put(Double.class, new Conversion<Double>() {
            public Double coerce(Object value) {
                if (value == null) return 0.0;
                if (value instanceof Number) return ((Number)value).doubleValue();
                if (value instanceof Boolean) return ((Boolean)value) ? -1.0 : 0.0;
                if (value instanceof String) return Double.parseDouble((String)value);
                throw new TypeConversionException(String.valueOf(value));
            }
        });
        conversionsByTargetType.put(String.class, new Conversion<String>() {
            public String coerce(Object value) {
                if (value == null) return null;
                return String.valueOf(value);
            }
        });
        CONVERSIONS_BY_TARGET_TYPE = Collections.unmodifiableMap(conversionsByTargetType);

//        Map<String, Operation<?>> operationsByOperator = new HashMap<String, Operation<?>>();
//        operationsByOperator.put("+", new Operation<Object>() {
//            @Override
//            public Object eval(Object leftValue, Object rightValue) {
//                Conversion<?> conversion = getConversion(leftValue, getConversion(String.class));
//                conversion = conversion.refine(rightValue);
//                conversion.coerce(leftValue)
//            }
//        })
//        OPERATIONS_BY_OPERATOR = Collections.unmodifiableMap(operationsByOperator);
    }

    private Eval() {} // pure static class

    public static <T> T coerce(Object value, Class<T> klass) {
        if (value != null && klass.isAssignableFrom(value.getClass())) return klass.cast(value);
        return getConversion(klass).coerce(value);
    }

    private static Conversion<?> getConversion(Object value, Conversion<?> nullConversion) {
        return value == null ? nullConversion : getConversion(value.getClass());
    }

    private static <T> Conversion<T> getConversion(Class<T> klass) {
        @SuppressWarnings({ "unchecked" })
        Conversion<T> conversion = (Conversion<T>)CONVERSIONS_BY_TARGET_TYPE.get(klass);
        if (conversion == null) throw new TypeConversionException("Unsupported conversion type: " + klass);
        return conversion;
    }

    abstract static class Conversion<T> {

        public abstract T coerce(Object value);

        public Conversion<?> refine(Object value) {
            return this;
        }
    }

//    abstract static class Operation<T> {
//        public abstract T eval(Object leftValue, Object rightValue);
//    }
}
