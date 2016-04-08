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

/**
 * Calculate the sum of numeric nodes specified in $what argument.
 *
 * JSON example:
 *
 * {"$function":"sum", "$what":[{"$value":"carCount"}, {"$value":"boatCount"}]}
 */
public class FunctionSum extends FunctionAbstract
{
    public static JsonNode evaluate(
        final JsonNode argsNode,
        final JsonNode valueNode,
        final ObjectTransformer transformer)
    throws ObjectTransformerException
    {
        Double sum = 0.0;
        JsonNode sumValuesArray = argsNode.path(ARGUMENT_WHAT);
        if (sumValuesArray.isArray())
        {
            for (JsonNode sumValue : sumValuesArray)
            {
                JsonNode value = transformer.transformValueNode(transformer.getSourceNode(), sumValue);
                if (value.isNumber())
                {
                    sum += value.asDouble();
                }
            }
        }

        // add start value if any
        if (valueNode.isNumber())
        {
            sum += valueNode.asDouble();
        }

        ObjectNode resultNode = (ObjectNode)argsNode;
        resultNode.put("returnValue", sum);

        return resultNode.get("returnValue");
    }
}
