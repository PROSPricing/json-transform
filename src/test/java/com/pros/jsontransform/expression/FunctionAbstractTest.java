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

import org.junit.Before;
import org.junit.Test;

import com.pros.jsontransform.ObjectTransformerException;

public class FunctionAbstractTest
{
    private static Object[] arguments;

    @Before
    public void setUpBefore() throws Exception
    {
        arguments = new Object[3];
        arguments[0] = new Integer(1);
        arguments[1] = new String("hello");
        arguments[2] = Function.$APPEND;
    }

    @Test
    public void test() throws ObjectTransformerException
    {
        Integer i = (Integer)FunctionAbstract.getArgument(0, Integer.class, arguments);
        String s = (String)FunctionAbstract.getArgument(1, String.class, arguments);
        Function e = (Function)FunctionAbstract.getArgument(2, Function.class, arguments);

        assertEquals("wrong integer", new Integer(1), i);
        assertEquals("wrong string", new String("hello"), s);
        assertEquals("wrong expression", Function.$APPEND, e);
    }

    @Test (expected=ObjectTransformerException.class)
    public void testWrongNumberOfArguments() throws ObjectTransformerException
    {
        Integer i = (Integer)FunctionAbstract.getArgument(3, Integer.class, arguments);
    }

    @Test (expected=ObjectTransformerException.class)
    public void testWrongClass() throws ObjectTransformerException
    {
        Integer i = (Integer)FunctionAbstract.getArgument(1, Integer.class, arguments);
    }

}
