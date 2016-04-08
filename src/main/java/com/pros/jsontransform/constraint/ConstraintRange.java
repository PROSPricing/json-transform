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

public class ConstraintRange
{
    public static void validate(
        final JsonNode constraintNode,
        final JsonNode resultNode,
        final ObjectTransformer transformer)
    throws ObjectTransformerException
    {
        boolean ltRangeValid = true;
        boolean gtRangeValid = true;
        JsonNode rangeNode = constraintNode.get(Constraint.$RANGE.toString());
        if (rangeNode.isObject())
        {
            JsonNode ltNode = rangeNode.path("less-than");
            if (!ltNode.isMissingNode())
            {
                if (resultNode.isNumber() && ltNode.isNumber())
                {
                    ltRangeValid = resultNode.asDouble() < ltNode.asDouble();
                }
                else if (resultNode.isTextual() && ltNode.isTextual())
                {
                    ltRangeValid = resultNode.asText().compareTo(ltNode.asText()) < 0;
                }
            }

            JsonNode gtNode = rangeNode.path("greater-than");
            if (!gtNode.isMissingNode())
            {
                if (resultNode.isNumber() && gtNode.isNumber())
                {
                    gtRangeValid = resultNode.asDouble() > gtNode.asDouble();
                }
                else if (resultNode.isTextual() && gtNode.isTextual())
                {
                    gtRangeValid = resultNode.asText().compareTo(gtNode.asText()) > 0;
                }
            }
        }

        if (ltRangeValid == false || gtRangeValid == false)
        {
            throw new ObjectTransformerException(
                "Constraint violation [" + Constraint.$RANGE.toString() + "]"
                + " on transform node "
                + transformer.getTransformNodeFieldName());
        }
    }
}
