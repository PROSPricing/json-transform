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

package com.pros.jsontransform.examples.arrays;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pros.jsontransform.JunitTools;
import com.pros.jsontransform.ObjectTransformer;
import com.pros.jsontransform.ObjectTransformerException;

public class ArraySortTest
{
    private static String fileNamePrefix;
    private static String pathToJson;
    private static String jsonSource;
    private static String jsonTransform;
    private static String jsonTarget;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        JunitTools.setUpBeforeClass();

        pathToJson = JunitTools.getPathToExamples() + "/arrays";
    }

    @Before
    public void setUpBefore()
    {
        fileNamePrefix = this.getClass().getSimpleName().replace("Test", "");
    }

    @Test
    public void testArrayMerge() throws IOException, ObjectTransformerException
    {
        jsonSource =    JunitTools.readFile(pathToJson + "/" + fileNamePrefix + "Source.json");
        jsonTransform = JunitTools.readFile(pathToJson + "/" + fileNamePrefix + "Map.json");
        jsonTarget =    JunitTools.readFile(pathToJson + "/" + fileNamePrefix + "Target.json");

        ObjectTransformer transformer = new ObjectTransformer(mapper);
        String result = transformer.transform(jsonSource, jsonTransform);

        System.out.println(result);

        assertTrue(mapper.readTree(result).equals(mapper.readTree(jsonTarget)));
    }
}
