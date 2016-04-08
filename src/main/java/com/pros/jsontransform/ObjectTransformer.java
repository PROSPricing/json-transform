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

package com.pros.jsontransform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pros.jsontransform.constraint.Constraint;
import com.pros.jsontransform.expression.Function;
import com.pros.jsontransform.filter.ArrayFilter;
import com.pros.jsontransform.plugin.PluginManager;
import com.pros.jsontransform.sort.ArraySort;

/**
 * Transform a source JSON tree into a target JSON tree.
 * It uses Jackson for JSON manipulations.
 */
public class ObjectTransformer
{
    /** Names of reserved transform directives */
    static final String COMMENT = "$comment";
    static final String PATH = "$path";
    static final String VALUE = "$value";
    static final String STRUCTURE = "$structure";
    static final String APPEND = "$append";
    static final String FILTER_INCLUDE = "$include";
    static final String FILTER_EXCLUDE = "$exclude";
    static final String OPERATOR = "$op";
    static final String EXPRESSION = "$expression";
    static final String EXPRESSION_FUNCTION = "$function";
    static final String EXPRESSION_$I = "$i";
    static final String CONSTRAINTS = "$constraints";
    static final String PATH_SEPARATOR = "|";
    static final String PATH_DOT = ".";
    static final String SORT = "$sort";

    /** Configuration properties */
    public Properties properties;

    /** The root node of the source JSON */
    JsonNode sourceRoot;

    /** The root node of the transform JSON */
    JsonNode transformRoot;

    /** The root node of the target JSON */
    ObjectNode targetRoot;

    /** The processed node of the source JSON */
    JsonNode sourceNode;

    /** The processed node of the transform JSON */
    JsonNode transformNode;

    /** The name of the transform node field being traversed */
    String transformNodeFieldName;

    /** The path to the source node computed by $path */
    String sourceNodePath;

    /** Keep track of node parents in the source tree */
    List<JsonNode> sourceNodeParents;

    /** Keep track of the index to visited elements in arrays in the source tree */
    List<Integer> sourceArrayIndexes;

    /** The Jackson object mapper */
    public ObjectMapper mapper;

    /** Plugin manager */
    private PluginManager pluginManager;

    /** Log tool */
    private static final Logger logger = Logger.getLogger(ObjectTransformer.class);

    public ObjectTransformer(
        final Properties properties,
        final ObjectMapper jacksonMapper)
    {
        this.properties = properties;
        this.mapper = jacksonMapper;
        this.pluginManager = new PluginManager(properties.getProperty("plugin.folder", "."));

        if (logger.getLevel() == null)
        {
            logger.setLevel(logger.getParent().getLevel());
        }
    }

    public ObjectTransformer(
        final ObjectMapper jacksonMapper)
    {
        this(new Properties(), jacksonMapper);
    }

    public Logger getLogger()
    {
        return logger;
    }

    public List<Integer> getSourceArrayIndexes()
    {
        return sourceArrayIndexes;
    }

    public int getIndexOfSourceArray()
    {
        return sourceArrayIndexes.get(sourceArrayIndexes.size() - 1);
    }

    public JsonNode getParentNode()
    {
        return sourceNodeParents.get(sourceNodeParents.size() - 1);
    }

    public JsonNode getSourceNode()
    {
        return sourceNode;
    }

    public String getTransformNodeFieldName()
    {
        return transformNodeFieldName;
    }

    public JsonNode transformValueNode(
        final JsonNode sourceNode,
        final JsonNode transformNode)
    throws ObjectTransformerException
    {
        JsonNode resultNode = sourceNode;

        // use $value to determine transformed value
        JsonNode valuePath = transformNode.get(VALUE);
        if (valuePath != null)
        {
            String valuePathAsString = valuePath.asText();
            if (!valuePathAsString.equalsIgnoreCase(PATH_DOT))
            {
                // $value contains a path to a source node
                resultNode = updateSourceFromPath(sourceNode, valuePath);
                restoreSourceFromPath(sourceNode, valuePath);
            }
        }

        return resultNode;
    }

    public JsonNode transformExpression(final JsonNode sourceNode, final JsonNode transformNode)
    throws ObjectTransformerException
    {
        JsonNode resultNode = transformValueNode(sourceNode, transformNode);
        JsonNode expressionNode = transformNode.path(EXPRESSION);
        if (expressionNode.isArray())
        {
            for (JsonNode functionNode : expressionNode)
            {
                // the first field name identifies the function name
                // e.g. {"$replace":{"$what":"Chr", "$with":"Lou"}}
                String functionName = functionNode.fieldNames().next();
                JsonNode arguments = functionNode.get(functionName);
                try
                {
                    Function function = Function.valueOf(functionName.toUpperCase());
                    resultNode = function.evaluate(arguments, resultNode, this);
                }
                catch (IllegalArgumentException iaEx)
                {
                    // function name may be a Java class that identifies a function plugin
                    String pluginClassName = functionName.replaceFirst("\\$", "");
                    resultNode = pluginManager.functionPluginEvaluate(
                        pluginClassName, arguments, resultNode, this);
                }
            }
        }

        // validate node constraints
        validateNode(resultNode, transformNode);

        return resultNode;
    }

    public String transform(
        final String sourceJson,
        final String transformJson)
    throws ObjectTransformerException, JsonProcessingException, IOException
    {
        // TODO in case of parse error cannot see which JSON fails
        sourceRoot = mapper.readTree(sourceJson);
        transformRoot = mapper.readTree(transformJson);

        return mapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(transform(sourceRoot, transformRoot));
    }

    public JsonNode transform(
        final JsonNode sourceRoot,
        final JsonNode transformRoot)
    throws ObjectTransformerException, JsonProcessingException, IOException
    {
        targetRoot = mapper.createObjectNode();

        transformNodeFieldName = "";
        sourceNodePath = "";
        sourceNodeParents = new ArrayList<JsonNode>();
        sourceArrayIndexes = new ArrayList<Integer>();

        // always have root as parent
        sourceNodeParents.add(sourceRoot);

        // start from root
        transformNode(sourceRoot, transformRoot, targetRoot);

        return targetRoot;
    }

    private void transformNode(
        final JsonNode sourceNode,
        final JsonNode transformNode,
        final ObjectNode targetNode)
    throws ObjectTransformerException
    {
        this.sourceNode = sourceNode;
        this.transformNode = transformNode;

        if (logger.getLevel() == Level.DEBUG)
        {
            logger.debug("transform " + transformNode.toString());
            logger.debug("source " + sourceNode.toString());
            logger.debug("source path " + this.sourceNodePath);
            for (JsonNode parent : sourceNodeParents)
            {
                int trunc = parent.toString().length() > 100 ? 100 : parent.toString().length();
                logger.debug("parent " + parent.toString().substring(0, trunc));
            }
        }

        // process $path directive
        JsonNode newSourceNode = updateSourceFromPath(sourceNode, transformNode.get(PATH));

        Iterator<String> fieldNames = transformNode.fieldNames();
        while (fieldNames.hasNext())
        {
            transformNodeFieldName = fieldNames.next();
            if (transformNodeFieldName.equalsIgnoreCase(COMMENT))
            {
                // ignore $comment nodes
                continue;
            }

            JsonNode transformChildNode = transformNode.get(transformNodeFieldName);
            if (transformChildNode.get(VALUE) != null || transformChildNode.get(EXPRESSION) != null)
            {
                // mapping value from transform map
                targetNode.put(
                    transformNodeFieldName,
                    transformExpression(newSourceNode, transformChildNode));
            }
            else if (transformChildNode.get(STRUCTURE) != null)
            {
                transformStructure(newSourceNode, transformChildNode, targetNode);
            }
            else if (transformChildNode.isObject())
            {
                transformObject(newSourceNode, transformChildNode, targetNode);
            }
            else if (transformChildNode.isArray())
            {
                transformArray(newSourceNode, transformChildNode, targetNode);
            }
            else if (!transformNodeFieldName.startsWith("$"))
            {
                // simple JSON field, copy from transform map
                targetNode.put(transformNodeFieldName, transformChildNode);
            }
        }

        // restore path
        restoreSourceFromPath(sourceNode, transformNode.get(PATH));
    }

    private void transformObject(
        final JsonNode sourceNode,
        final JsonNode transformNode,
        final ObjectNode targetNode)
    throws ObjectTransformerException
    {
        ObjectNode childNode = mapper.createObjectNode();
        targetNode.replace(transformNodeFieldName, childNode);

        // visit child object
        transformNode(sourceNode, transformNode, childNode);
    }

    private void transformStructure(
        final JsonNode sourceNode,
        final JsonNode transformNode,
        final ObjectNode targetNode)
    throws ObjectTransformerException
    {
        // process $path directive
        JsonNode newSourceNode = updateSourceFromPath(sourceNode, transformNode.get(PATH));

        JsonNode structureNode = transformNode.get(STRUCTURE);
        if (structureNode.isObject())
        {
            // mapping an object
            ObjectNode childNode =
                (ObjectNode)targetNode.get(transformNode.path(APPEND).asText());
            if (childNode == null)
            {
                // no $append directive found, need new object
                childNode = mapper.createObjectNode();
                targetNode.replace(transformNodeFieldName, childNode);
            }

            // visit child object
            transformNode(newSourceNode, structureNode, childNode);
        }
        else if (structureNode.isArray())
        {
            // mapping an array
            ArrayNode childNode =
                (ArrayNode)targetNode.get(transformNode.path(APPEND).asText());
            if (childNode == null)
            {
                // no $append directive found, need new object
                childNode = mapper.createArrayNode();
                targetNode.replace(transformNodeFieldName, childNode);
            }

            processArray(newSourceNode, transformNode, childNode);
        }

        // restore path
        restoreSourceFromPath(sourceNode, transformNode.get(PATH));
    }

    private void transformArray(
        final JsonNode sourceNode,
        final JsonNode transformNode,
        final ObjectNode targetNode)
    throws ObjectTransformerException
    {
        // create new array
        ArrayNode targetArray = mapper.createArrayNode();
        targetNode.replace(transformNodeFieldName, targetArray);

        processArray(sourceNode, transformNode, targetArray);
    }

    private void processArray(
        final JsonNode sourceNode,
        final JsonNode transformNode,
        final ArrayNode targetArray)
    throws ObjectTransformerException
    {
        // add array index
        sourceArrayIndexes.add(new Integer(-1));
        int lastIndex = sourceArrayIndexes.size() - 1;

        // use $structure if any
        JsonNode transformArray = transformNode.get(STRUCTURE);
        if (transformArray == null)
        {
            transformArray = transformNode;
        }

        if (sourceNode.isArray())
        {
            // target array is based on source array
            int count = 0;
            JsonNode transformElement = transformArray.path(0);
            for (JsonNode sourceArrayNode : sourceNode)
            {
                if (includeArrayNode(sourceArrayNode, transformNode))
                {
                    // increment array index to point to new node
                    sourceArrayIndexes.set(lastIndex, sourceArrayIndexes.get(lastIndex) + 1);

                    // add parent
                    sourceNodeParents.add(sourceArrayNode);

                    // update source path
                    sourceNodePath += PATH_SEPARATOR + count;

                    if (transformElement.has(VALUE) || transformElement.has(EXPRESSION))
                    {
                       // simple values transform
                       targetArray.add(transformExpression(sourceArrayNode, transformElement));
                    }
                    else
                    {
                        ObjectNode targetElement = mapper.createObjectNode();
                        targetArray.add(targetElement);

                        // visit array element, use transform array first element as model
                        transformNode(sourceArrayNode, transformElement, targetElement);
                    }

                    // remove parent
                    sourceNodeParents.remove(sourceNodeParents.size() - 1);

                    // restore source path
                    sourceNodePath = sourceNodePath.substring(0, sourceNodePath.lastIndexOf(PATH_SEPARATOR));
                }
            }

            // restore sourceNode to array node
            this.sourceNode = sourceNode;

            // sort directive
            sortArray(targetArray, transformNode);
        }
        else
        {
            // process each element of transform array
            for (JsonNode childElementNode : transformArray)
            {
                if (childElementNode.get(VALUE) != null
                    || childElementNode.get(EXPRESSION) != null)
                {
                    // simple values
                    targetArray.add(transformExpression(sourceNode, childElementNode));
                }
                else if (childElementNode.isObject())
                {
                    // object values
                    ObjectNode targetElement = mapper.createObjectNode();
                    targetArray.add(targetElement);

                    // visit array element
                    transformNode(sourceNode, childElementNode, targetElement);
                }
                else if (childElementNode.isArray())
                {
                    // TODO nested arrays
                }
                else
                {
                    // copy map value to target
                    targetArray.add(childElementNode);
                }
            }
        }

        // remove array index
        sourceArrayIndexes.remove(lastIndex);
    }

    private boolean includeArrayNode(
        final JsonNode sourceArrayNode,
        final JsonNode transformNode)
    throws ObjectTransformerException
    {
        boolean include = false;

        // includes
        JsonNode includeFilterNode = transformNode.path(FILTER_INCLUDE);
        if (includeFilterNode.isArray())
        {
            for (JsonNode filterNode : includeFilterNode)
            {
                if (filterResult(filterNode, sourceArrayNode))
                {
                    include = true;
                    break;
                }
            }
        }
        else
        {
            include = true;
        }

        // excludes
        JsonNode excludeFilterNode = transformNode.path(FILTER_EXCLUDE);
        if (excludeFilterNode.isArray())
        {
            for (JsonNode filterNode : excludeFilterNode)
            {
                if (filterResult(filterNode, sourceArrayNode))
                {
                    include = false;
                    break;
                }
            }
        }

        return include;
    }

    private boolean filterResult(
        final JsonNode filterNode,
        final JsonNode sourceArrayNode)
    throws ObjectTransformerException
    {
        boolean result;

        // the first field name identifies the filter name
        // e.g. {"$contains":{"$value":"name", "$what":"txt"}}
        String filterName = filterNode.fieldNames().next();
        try
        {
            ArrayFilter filter = ArrayFilter.valueOf(filterName.toUpperCase());
            result = filter.evaluate(filterNode, sourceArrayNode, this);
        }
        catch (IllegalArgumentException iaEx)
        {
            // filter name may be a Java class that identifies a filter plugin
            String pluginClassName = filterName.replaceFirst("\\$", "");
            result = pluginManager.filterPluginEvaluate(
                pluginClassName, filterNode, sourceArrayNode, this);
        }

        return result;
    }

    private JsonNode updateSourceFromPath(
        final JsonNode sourceNode,
        final JsonNode pathNode)
    throws ObjectTransformerException
    {
        // use transformNode $path or $value to find the source node
        JsonNode resultNode = sourceNode;
        if (pathNode != null)
        {
            // remember current parent index
            int parentIndex = sourceNodeParents.size() - 1;

            // e.g path: items|0|items|0|items
            String regExp = "[" + PATH_SEPARATOR + "]";
            String[] pathParts = pathNode.asText().split(regExp);
            for (String part : pathParts)
            {
                if (!part.equalsIgnoreCase(".."))
                {
                    // reset pointer to current parent
                    parentIndex = sourceNodeParents.size() - 1;
                }

                // $i in path refers to current array index
                if (sourceArrayIndexes.size() > 0)
                {
                    part = part.replace(
                        EXPRESSION_$I,
                        String.valueOf(sourceArrayIndexes.get(sourceArrayIndexes.size() - 1)));
                }

                if (part.equalsIgnoreCase("..") && --parentIndex >= 0)
                {
                    // parent object
                    resultNode = sourceNodeParents.get(parentIndex);
                }
                else if (resultNode.isArray())
                {
                    try
                    {
                        // handle array indexes in path
                        int index = Integer.parseInt(part);

                        // array element access
                        resultNode = resultNode.path(index);
                    }
                    catch (Exception e)
                    {
                        if (part.contains("="))
                        {
                            // array search by fieldname=value
                            String [] searchParts = part.split("=");
                            for (JsonNode elementNode : resultNode)
                            {
                                if (elementNode.path(searchParts[0]).asText().contains(searchParts[1]))
                                {
                                    resultNode = elementNode;
                                    break;
                                }
                            }
                        }
                    }
                }
                else if (part.isEmpty() && pathParts.length > 1)
                {
                    // absolute path to source root
                    resultNode = sourceRoot;
                }
                else if (resultNode.isObject())
                {
                    // find by field name
                    resultNode = resultNode.path(part);
                }
                else
                {
                    if (Boolean.valueOf(properties.getProperty(
                        "exception.on.path.resolution", "false")) == true)
                    {
                        throw new ObjectTransformerException(
                            "Cannot resolve path " + pathNode.asText() 
                            + " for source node " + sourceNode.toString());
                    }
                }

                sourceNodeParents.add(resultNode);
            }

            // update path
            sourceNodePath += PATH_SEPARATOR + pathNode.asText();
        }

        return resultNode;
    }

    @SuppressWarnings("unused")
    private void restoreSourceFromPath(
        final JsonNode sourceNode,
        final JsonNode pathNode)
    throws ObjectTransformerException
    {
        if (pathNode != null)
        {
            // e.g path structure: items|0|items|0|items
            int lastParentIndex = sourceNodeParents.size() - 1;
            String regExp = "[" + PATH_SEPARATOR + "]";
            String[] pathParts = pathNode.asText().split(regExp);
            for (String part : pathParts)
            {
                sourceNodeParents.remove(lastParentIndex--);
            }

            sourceNodePath = sourceNodePath.substring(
                0, sourceNodePath.lastIndexOf(PATH_SEPARATOR + pathNode.asText()));

            this.sourceNode = sourceNode;
        }
    }

    private void validateNode(
        final JsonNode resultNode,
        final JsonNode transformNode)
    throws ObjectTransformerException
    {
        JsonNode constraintsArray = transformNode.path(CONSTRAINTS);
        if (constraintsArray.isArray())
        {
            for (JsonNode constraintNode : constraintsArray)
            {
                // the first field name identifies the constraint name
                // e.g. "$constraints":[{"$required":true}, {"$type":"string"}, {"$values":["a","b","c"]}]
                String constraintName = constraintNode.fieldNames().next();
                try
                {
                    Constraint constraint = Constraint.valueOf(constraintName.toUpperCase());
                    constraint.validate(constraintNode, resultNode, this);
                }
                catch (IllegalArgumentException iaEx)
                {
                    // contraint name may be a Java class that identifies a constraint plugin
                    String pluginClassName = constraintName.replaceFirst("\\$", "");
                    pluginManager.constraintPluginValidate(
                        pluginClassName, constraintNode, resultNode, this);
                }
            }
        }
    }

    private void sortArray(
        final ArrayNode targetArray,
        final JsonNode transformNode)
    throws ObjectTransformerException
    {
        JsonNode sortNode = transformNode.get(SORT);
        if (sortNode != null)
        {
            // the first field name identifies the sort handler
            // {"$sort":{"$ascending":{"$by":{"$value":"."}}}}
            String sortName = sortNode.fieldNames().next();
            try
            {
                ArraySort sortHandler = ArraySort.valueOf(sortName.toUpperCase());
                sortHandler.sort(targetArray, sortNode, this);
            }
            catch (IllegalArgumentException iaEx)
            {
                // sort handler may be a Java class that identifies a sort plugin
                String pluginClassName = sortName.replaceFirst("\\$", "");
                pluginManager.sortPluginSort(
                    pluginClassName, targetArray, sortNode, this);
            }
        }
    }
}
