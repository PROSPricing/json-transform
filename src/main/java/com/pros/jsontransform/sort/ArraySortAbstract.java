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

import java.util.ArrayList;
import java.util.Comparator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.pros.jsontransform.ObjectTransformer;
import com.pros.jsontransform.ObjectTransformerException;

public class ArraySortAbstract
{
    static int compareValueNodes(JsonNode value1, JsonNode value2)
    {
        int result = 0;

        if (value1.isNumber() && value2.isNumber())
        {
            result = Double.compare(value1.asDouble(), value2.asDouble());
        }
        else if (value1.isTextual() && value2.isTextual())
        {
            result = value1.asText().compareTo(value2.asText());
        }

        return result;
    }

    static void doSort(
        final ArrayNode arrayNode,
        final JsonNode sortNode,
        final ObjectTransformer transformer)
    {
        // move array nodes to sorted array
        int size = arrayNode.size();
        ArrayList<JsonNode> sortedArray = new ArrayList<JsonNode>(arrayNode.size());
        for (int i = 0; i < size; i++)
        {
            sortedArray.add(arrayNode.remove(0));
        }

        // sort array
        sortedArray.sort(new NodeComparator(sortNode, transformer));

        // move nodes back to targetArray
        for (int i = 0; i < sortedArray.size(); i++)
        {
            arrayNode.add(sortedArray.get(i));
        }
    }

    private static class NodeComparator implements Comparator<JsonNode>
    {
        private ArraySort sortDirection;
        private JsonNode byNode;
        private ObjectTransformer transformer;

        NodeComparator(final JsonNode sortNode, final ObjectTransformer transformer)
        {
            // e.g. sortNode {"$ascending":{"$by":{"$value":"."}}}
            this.sortDirection = ArraySort.valueOf(sortNode.fieldNames().next().toUpperCase());
            this.byNode = sortNode.get(sortDirection.name().toLowerCase()).get(ArraySort.ARGUMENT_BY);
            this.transformer = transformer;
        }

        @Override
        public int compare(JsonNode node1, JsonNode node2)
        {
            int result = 0;
            try
            {
                // use byNode to find values in node1 and node2
                JsonNode value1 = transformer.transformExpression(node1, byNode);
                JsonNode value2 = transformer.transformExpression(node2, byNode);

                // ascending order
                result = sortDirection == ArraySort.$ASCENDING 
                    ? compareValueNodes(value1, value2)
                    : compareValueNodes(value2, value1);
            }
            catch (ObjectTransformerException ex)
            {
                transformer.getLogger().error(ex);
            }

            return result;
        }
    }

}
