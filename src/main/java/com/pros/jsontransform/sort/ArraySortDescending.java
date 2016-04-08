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

package com.pros.jsontransform.sort;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.pros.jsontransform.ObjectTransformer;
import com.pros.jsontransform.ObjectTransformerException;

/**
 * Process ascending sort
 * {"$descending":{"$by":{"$value":"."}}}
 */
public class ArraySortDescending extends ArraySortAbstract
{
    public static void sort(
        final ArrayNode arrayNode,
        final JsonNode sortNode,
        final ObjectTransformer transformer)
    throws ObjectTransformerException
    {
        // check $ascending arguments
        if (sortNode.get(ArraySort.$DESCENDING.name().toLowerCase()).get(ArraySort.ARGUMENT_BY) == null)
        {
            throw new ObjectTransformerException(
                "Missing argument " + ArraySort.ARGUMENT_BY
                + " in sort directive " + sortNode.toString());
        }

        doSort(arrayNode, sortNode, transformer);
    }
}
