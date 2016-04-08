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

public class ConstraintValues
{
    public static void validate(
        final JsonNode constraintNode,
        final JsonNode resultNode,
        final ObjectTransformer transformer)
    throws ObjectTransformerException
    {
        boolean found = false;
        JsonNode valuesArray = constraintNode.get(Constraint.$VALUES.toString());
        if (valuesArray.isArray())
        {
            for (JsonNode valueNode : valuesArray)
            {
                if (resultNode.equals(valueNode))
                {
                    found = true;
                    break;
                }
            }
        }

        if (!found)
        {
            throw new ObjectTransformerException(
                "Constraint violation [" + Constraint.$VALUES.toString() + "]"
                + " on transform node "
                + transformer.getTransformNodeFieldName());
        }
    }
}
