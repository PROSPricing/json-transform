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

/**
 * The constraint types.
 */
public enum Constraint
{
    $RANGE
    {
        @Override
        public void validate(
            final JsonNode constraintNode,
            final JsonNode resultNode,
            final ObjectTransformer transformer)
        throws ObjectTransformerException
        {
            ConstraintRange.validate(constraintNode, resultNode, transformer);
        }
    },
    $REQUIRED
    {
        @Override
        public void validate(
            final JsonNode constraintNode,
            final JsonNode resultNode,
            final ObjectTransformer transformer)
        throws ObjectTransformerException
        {
            ConstraintRequired.validate(constraintNode, resultNode, transformer);
        }
    },
    $TYPE
    {
        @Override
        public void validate(
            final JsonNode constraintNode,
            final JsonNode resultNode,
            final ObjectTransformer transformer)
        throws ObjectTransformerException
        {
            ConstraintType.validate(constraintNode, resultNode, transformer);
        }
    },
    $VALUES
    {
        @Override
        public void validate(
            final JsonNode constraintNode,
            final JsonNode resultNode,
            final ObjectTransformer transformer)
        throws ObjectTransformerException
        {
            ConstraintValues.validate(constraintNode, resultNode, transformer);
        }
    };

    public abstract void validate(
        final JsonNode constraintNode,
        final JsonNode resultNode,
        final ObjectTransformer transformer)
    throws ObjectTransformerException;

    @Override
    public String toString()
    {
        return name().toLowerCase();
    }
}
