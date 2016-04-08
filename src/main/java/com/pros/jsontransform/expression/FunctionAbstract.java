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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pros.jsontransform.ObjectTransformer;
import com.pros.jsontransform.ObjectTransformerException;

public abstract class FunctionAbstract
{
    static final String ARGUMENT_WHAT = "$what";
    static final String ARGUMENT_WITH = "$with";
    static final String $I = "$i";

    @SuppressWarnings("rawtypes")
    static Object getArgument(
        final int argPosition,
        final Class argClass,
        final Object ... argArray)
    throws ObjectTransformerException
    {
        if (argPosition >= argArray.length)
        {
            throw new ObjectTransformerException("Wrong number of arguments.");
        }
        if (argArray[argPosition] != null && !argClass.isInstance(argArray[argPosition]))
        {
            throw new ObjectTransformerException("Wrong argument type.");
        }
        return argArray[argPosition];
    }

    static String transformValue(
        final JsonNode valueNode,
        final ObjectTransformer transformer)
    {
        String result = "";
        if (valueNode != null)
        {
            result = valueNode.asText();
        }
        return result;
    }

    static JsonNode transformArgument(
        final JsonNode argumentNode,
        final ObjectTransformer transformer)
    throws ObjectTransformerException
    {
        ObjectNode resultNode = transformer.mapper.createObjectNode();

        if (argumentNode.isContainerNode())
        {
            // transform argument node
            JsonNode sourceNode = transformer.getSourceNode();
            JsonNode valueNode = transformer.transformExpression(sourceNode, argumentNode);
            resultNode.put("result", valueNode);
        }
        else if (argumentNode.isTextual())
        {
            // transform $i modifier
            String textValue = argumentNode.textValue();
            if (textValue.contains($I))
            {
                int arrayIndex = transformer.getIndexOfSourceArray();
                textValue = textValue.replace($I, String.valueOf(arrayIndex));
            }
            resultNode.put("result", textValue);
        }
        else
        {
            resultNode.put("result", argumentNode);
        }

        return resultNode.get("result");
    }
}
