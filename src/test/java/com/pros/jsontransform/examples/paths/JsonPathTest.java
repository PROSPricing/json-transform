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

package com.pros.jsontransform.examples.paths;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import com.pros.jsontransform.JunitTools;
import com.pros.jsontransform.ObjectTransformerException;

/**
 * Work in progress... testing the possibility to integrate
 * JsonPath into json-transform as a path processor.
 */
public class JsonPathTest
{
    private static String pathToJson;
    private static String fileNamePrefix;
    private static String jsonSource;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        pathToJson = JunitTools.getPathToExamples() + "/paths";
    }

    @Before
    public void setUpBefore()
    {
        fileNamePrefix = this.getClass().getSimpleName().replace("Test", "");
    }

    @Test
    public void test() throws IOException, ObjectTransformerException
    {
        jsonSource =    JunitTools.readFile(pathToJson + "/" + fileNamePrefix + "Source.json");

        Configuration.setDefaults(new Configuration.Defaults()
        {
            private final JsonProvider jsonProvider = new JacksonJsonNodeJsonProvider();
            private final MappingProvider mappingProvider = new JacksonMappingProvider();

            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }
        });

        Object document = Configuration.defaultConfiguration().jsonProvider().parse(jsonSource);

        // get first author
        JsonNode author0 = JsonPath.read(document, "$.store.book[0].author");

        // get Moby Dick book and author
        JsonNode mobyDickBook = JsonPath.read(document, "$..book[?(@.title == 'Moby Dick')]]");
        JsonNode mobyDickAuthor = JsonPath.read(mobyDickBook, "$[0].author");

        System.out.println(jsonSource);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(author0));
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mobyDickBook));
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mobyDickAuthor));
    }
}
