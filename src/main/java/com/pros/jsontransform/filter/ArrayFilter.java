/*
 * Copyright (c) 2016 PROS, Inc.
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

package com.pros.jsontransform.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.pros.jsontransform.ObjectTransformer;
import com.pros.jsontransform.ObjectTransformerException;

/**
 * The JSON  array filter operators.
 */
public enum ArrayFilter
{
    $CONTAINS
    {
        @Override
        public boolean evaluate(
            final JsonNode filterNode,
            final JsonNode elementNode,
            final ObjectTransformer transformer)
        throws ObjectTransformerException
        {
            return ArrayFilterContains.evaluate(filterNode, elementNode, transformer);
        }
    },
    $EQUALS
    {
        @Override
        public boolean evaluate(
            final JsonNode filterNode,
            final JsonNode elementNode,
            final ObjectTransformer transformer)
        throws ObjectTransformerException
        {
            return ArrayFilterEquals.evaluate(filterNode, elementNode, transformer);
        }
    };

    public static String ARGUMENT_VALUE = "$value";
    public static String ARGUMENT_WHAT = "$what";

    public abstract boolean evaluate(
        final JsonNode filterNode,
        final JsonNode elementNode,
        final ObjectTransformer transformer)
    throws ObjectTransformerException;
}
