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

package com.pros.jsontransform.expression;

import com.fasterxml.jackson.databind.JsonNode;
import com.pros.jsontransform.ObjectTransformer;
import com.pros.jsontransform.ObjectTransformerException;

/**
 * The transform functions.
 *
 * TODO function argument validation
 */
public enum Function
{
    $APPEND
    {
        @Override
        public JsonNode evaluate(
            final JsonNode argsNode,
            final JsonNode valueNode,
            final ObjectTransformer transformer)
        throws ObjectTransformerException
        {
            return FunctionAppend.evaluate(argsNode, valueNode, transformer);
        }
    },
    $APPEND_ARRAY_INDEX
    {
        @Override
        public JsonNode evaluate(
            final JsonNode argsNode,
            final JsonNode valueNode,
            final ObjectTransformer transformer)
        throws ObjectTransformerException
        {
            return FunctionAppendArrayIndex.evaluate(argsNode, valueNode, transformer);
        }
    },
    $RANDOM_UUID
    {
        @Override
        public JsonNode evaluate(
            final JsonNode argsNode,
            final JsonNode valueNode,
            final ObjectTransformer transformer)
        throws ObjectTransformerException
        {
            return FunctionRandomUUID.evaluate(argsNode, valueNode, transformer);
        }
    },
    $REPLACE
    {
        @Override
        public JsonNode evaluate(
            final JsonNode argsNode,
            final JsonNode valueNode,
            final ObjectTransformer transformer)
        throws ObjectTransformerException
        {
            return FunctionReplace.evaluate(argsNode, valueNode, transformer);
        }
    },
    $SET
    {
        @Override
        public JsonNode evaluate(
            final JsonNode argsNode,
            final JsonNode valueNode,
            final ObjectTransformer transformer)
        throws ObjectTransformerException
        {
            return FunctionSet.evaluate(argsNode, valueNode, transformer);
        }
    },
    $SUM
    {
        @Override
        public JsonNode evaluate(
            final JsonNode argsNode,
            final JsonNode valueNode,
            final ObjectTransformer transformer)
        throws ObjectTransformerException
        {
            return FunctionSum.evaluate(argsNode, valueNode, transformer);
        }
    };

    public abstract JsonNode evaluate(
        final JsonNode argsNode,
        final JsonNode valueNode,
        final ObjectTransformer transformer)
    throws ObjectTransformerException;
}
