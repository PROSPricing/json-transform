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

package com.pros.jsontransform.constraint;

import com.fasterxml.jackson.databind.JsonNode;
import com.pros.jsontransform.ObjectTransformer;
import com.pros.jsontransform.ObjectTransformerException;

public class ConstraintType
{
    public static void validate(
        final JsonNode constraintNode,
        final JsonNode resultNode,
        final ObjectTransformer transformer)
    throws ObjectTransformerException
    {
        String type = constraintNode.get(Constraint.$TYPE.toString()).asText().toUpperCase();
        if (!JsonNodeType.valueOf(type).isValid(resultNode))
        {
            throw new ObjectTransformerException(
                "Constraint violation [" + Constraint.$TYPE.toString() + "]"
                + " on transform node "
                + transformer.getTransformNodeFieldName());
        }
    }

    enum JsonNodeType
    {
        NUMBER  { @Override boolean isValid(JsonNode node) {return node.isNumber();} },
        STRING  { @Override boolean isValid(JsonNode node) {return node.isTextual();} },
        BOOLEAN { @Override boolean isValid(JsonNode node) {return node.isBoolean();} },
        OBJECT  { @Override boolean isValid(JsonNode node) {return node.isObject();} },
        ARRAY   { @Override boolean isValid(JsonNode node) {return node.isArray();} };

        abstract boolean isValid(JsonNode node);

        @Override
        public String toString()
        {
            return name().toLowerCase();
        }
    }
}
