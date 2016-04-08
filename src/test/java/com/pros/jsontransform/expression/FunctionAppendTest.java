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

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pros.jsontransform.ObjectTransformer;
import com.pros.jsontransform.ObjectTransformerException;

public class FunctionAppendTest
{
    private static ObjectMapper mapper = new ObjectMapper();
    private static ObjectTransformer transformer = new ObjectTransformer(mapper);

    @Test
    public void test() throws ObjectTransformerException, JsonProcessingException, IOException
    {
        JsonNode object = mapper.readTree
            ( "{"
            + "  \"value\":\"hello\","
            + "  \"args\":{\"$what\":\" world\"}"
            + "}"
            );

        JsonNode result = FunctionAppend.evaluate(
            object.get("args"),
            object.get("value"),
            transformer);

        assertEquals("wrong result", "hello world", result.asText());
    }

}
