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

package com.pros.jsontransform.plugin;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.pros.jsontransform.ObjectTransformer;
import com.pros.jsontransform.ObjectTransformerException;

/**
 * Manage json-transform plugins
 */
public class PluginManager
{
    /** The class loader */
    private URLClassLoader urlClassLoader;

    /** Where plugins are located */
    private String pluginFolder;

    public PluginManager(final String pluginFolder)
    {
        this.pluginFolder = pluginFolder;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void constraintPluginValidate(
        final String pluginClassName,
        final JsonNode constraintNode,
        final JsonNode resultNode,
        final ObjectTransformer transformer)
    throws ObjectTransformerException
    {
        try
        {
            Class pluginClass = loadPlugin(pluginClassName);
            Method validate = pluginClass.getMethod(
                "validate",
                JsonNode.class,
                JsonNode.class,
                ObjectTransformer.class);
            validate.invoke(null, constraintNode, resultNode, transformer);
        }
        catch (Exception ex)
        {
            throw new ObjectTransformerException(
                "Error from constraint plugin " + pluginClassName, ex);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public JsonNode functionPluginEvaluate(
        final String pluginClassName,
        final JsonNode argsNode,
        final JsonNode resultNode,
        final ObjectTransformer transformer)
    throws ObjectTransformerException
    {
        try
        {
            Class pluginClass = loadPlugin(pluginClassName);
            Method evaluate = pluginClass.getMethod(
                "evaluate",
                JsonNode.class,
                JsonNode.class,
                ObjectTransformer.class);
            return (JsonNode)evaluate.invoke(null, argsNode, resultNode, transformer);
        }
        catch (Exception ex)
        {
            throw new ObjectTransformerException(
                "Error from function plugin " + pluginClassName, ex);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public boolean filterPluginEvaluate(
        final String pluginClassName,
        final JsonNode filterNode,
        final JsonNode elementNode,
        final ObjectTransformer transformer)
    throws ObjectTransformerException
    {
        try
        {
            Class pluginClass = loadPlugin(pluginClassName);
            Method evaluate = pluginClass.getMethod(
                "evaluate",
                JsonNode.class,
                JsonNode.class,
                ObjectTransformer.class);
            return (boolean)evaluate.invoke(null, filterNode, elementNode, transformer);
        }
        catch (Exception ex)
        {
            throw new ObjectTransformerException(
                "Error from filter plugin " + pluginClassName, ex);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void sortPluginSort(
        final String pluginClassName,
        final ArrayNode targetArray,
        final JsonNode sortNode,
        final ObjectTransformer transformer)
    throws ObjectTransformerException
    {
        try
        {
            Class pluginClass = loadPlugin(pluginClassName);
            Method sort = pluginClass.getMethod(
                "sort",
                ArrayNode.class,
                JsonNode.class,
                ObjectTransformer.class);
            sort.invoke(null, targetArray, sortNode, transformer);
        }
        catch (Exception ex)
        {
            throw new ObjectTransformerException(
                "Error from sort plugin " + pluginClassName, ex);
        }
    }

    @SuppressWarnings("rawtypes")
    private Class loadPlugin(final String pluginClassName)
    throws ObjectTransformerException
    {
        Class pluginClass = null;

        try
        {
            if (urlClassLoader == null)
            {
                File folder = new File(pluginFolder); 
                File[] listOfFiles = folder.listFiles();
                int jarCount = 0;
                URL[] jarUrls = new URL[listOfFiles.length];
                for (int i = 0; i < listOfFiles.length; i++)
                {
                    File jarFile = listOfFiles[i];
                    if (jarFile.isFile() && jarFile.getName().endsWith(".jar"))
                    {
                        jarUrls[jarCount++] = jarFile.toURI().toURL();
                    }
                }
                if (jarCount == 0)
                {
                    throw new ObjectTransformerException(
                        "Cannot load plugin " + pluginClassName +
                        " No jars found in plugin folder " + pluginFolder);
                }
                urlClassLoader = new URLClassLoader(jarUrls);
            }
            pluginClass = urlClassLoader.loadClass(pluginClassName);
        }
        catch (Exception ex)
        {
            throw new ObjectTransformerException(
                "Cannot load plugin " + pluginClassName, ex);
        }

        return pluginClass;
    }
}
