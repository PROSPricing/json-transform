# json-transform

A light-weight Java library that supports JSON transformation (mapping), so that a source JSON is transformed into a target JSON. The transformation process is guided by a JSON that defines each mapping step. In addition to structural transformation it is possible to specify validity constraints on the source JSON such as node existence, data type, allowed values, ranges, etc.

Some use cases where JSON transformation is useful are:

- System integration
- REST API development
- Structure normalization
- Data enrichment
- One to many, many to one mappings

In these scenarios, JSON mapping and validation is very common and typically requires writing lots of custom code that is tightly coupled to backend systems and data models, with many known drawbacks. **json-transform** reduces mapping code dramatically by specifying mappings, transformations and validation in a declarative way (JSON), so they can be automatically processed without custom code.

## Features

- Simple and intuitive transformation definition in JSON
- Work with objects and arrays
- Source structure references with simplified JSON path
- Transform values with expressions
- Transform existing structures
- Create new structures
- Include / exclude array elements
- Validate node existence, data type, values, etc.
- Extensibility via plugin framework

## How It Works

As an example, a JSON that contains the separate arrays *firstNames* and *lastNames* is transformed into a single array of *people* who have a *fullName*. Below is the source JSON.

**The source JSON**

```javascript
{
    "firstNames": ["Paul", "Hanna", "Peter"],
    "lastNames": ["Jones", "Montana", "Russell"]
}
```

**The transform map**

The next step requires writing the transform map. The transform map describes the target JSON and includes transformation directives that allow to retrieve and modify data from the source JSON and include it in the target JSON as desired.

```javascript
{
    "$comment":
    [
        "Create one target array from two source arrays. ",
        "By setting $path to a source array the transformation loops ",
        "through each source element and creates a target element ",
        "using the transform array first element as a model. ",
        "The $value resolves references to firstNames and lastNames."
    ],
    "$path":"firstNames",
    "people":
    [
        {
            "fullName":
            {
                "$value":".",
                "$expression":
                [
                    {"$append":{"$what":" "}},
                    {
                        "$append":
                        {
                            "$what":
                            {
                               "$value":"..|..|lastNames|$i",
                               "$constraints":[{"$required":true}, {"$type":"string"}]
                            }
                        }
                    }
                ]
            }
        }
    ]
}
```

Transformation directives start with a $. In the above example *$path* sets the source data context to the *firstNames* array, *$value* retrieves a first name from the source JSON array, *$expression* and *$function* transform the value before it is added to the target JSON. The *$constraints* directive makes sure that each first name has corresponding last name of type string.

**The target JSON**

The result of the transformation process is the target JSON.

```javascript
{
  "people" : [ {
    "fullName" : "Paul Jones"
  }, {
    "fullName" : "Hanna Montana"
  }, {
    "fullName" : "Peter Russell"
  } ]
}
```

## The $ Transformation Directives

| Directive     | Description   |
| ------------- | ------------- |
| $path         | Identifies a JSON node in the source data. The node becomes the transformation context. |
| $value        | Same as $path without changing the transformation context. |
| $structure    | Specifies whether the target JSON is an object or an array. Mostly used to process arrays. |
| $append       | Appends fields or array elements to objects or arrays already created in the target JSON. |
| $include      | Specifies rules for including source array elements in the target array. |
| $exclude      | Specifies rules for excluding source array elements from the target array. |
| $sort         | Specifies an array sorting criteria. Can refer to array element fields as well as use expressions. |
| $expression   | Applies a set of functions that transform a value. |
| $function     | Modifies a value, return values, etc... Many functions are chained into expressions. |
| $i            | Refers to the index in the array being processed within current context. Used in $path, $value, $function. |
| $comment      | Can be inserted anywhere in the transform map to document transformations. |

**$PATH and $VALUE Examples**

The *$path* directive sets the transformation context to a specific node in the source data. The special character *|* is the separator of the path parts. Each part can be 1) a field name, 2) a numeric value that identifies a specific array element 3) an expression *<name>=<value>* that identifies an array element by an element field value, 4) a parent node.

| Example | Description |
| ------- | ----------- |
| "$path" : "person"            | Set transformation context to the field *person* (relative path). |
| "$path" : "\|person\|address" | Set context to the object *person*, field *address* starting from the source root object (absolute path). |
| "$path" : "prices\|1\|tax"            | Set context to the array *prices*, element *1* (second element), field *tax*. |
| "$path" : "cars\|color=red"           | Set context to the red car in the *cars* array. |
| "$path" : "items\|0\|items\|0\|items" | Set context the *items* array by navigating a structure of nested arrays. |
| "$path" : "..\|lastName"              | Set context the current node sibling *lastName* through its parent *..* |
| "$path" : "..\|colors\|$i"    | While processing an array element, set context to the sibling array *colors* at same element. |

The *$value* directive uses the same syntax as *$path* and returns a value (JSON node) without changing the transformation context. In addition to the examples above, *"$value":"."* indicates the node that is currently being processed, e.g. when processing an array.

## The $ Constraints Directives

| Directive     | Description   |
| ------------- | ------------- |
| $constraints  | Specifies a set of constraints to be applied to a source JSON node value or an expression result. |
| $range        | A value must be within a specified range. |
| $required     | A value is required to be present in the source JSON. |
| $type         | A value is expected to be of the specified JSON type. |
| $values       | A value must be in a set of predefined possible values. |

**$CONSTRAINTS Examples**

The *$constraints* directive sets multiple validation rules on a JSON node. Range, type, values and required constraints can be used to define a node validation criteria. Constraints typically apply to nodes in the source JSON, but in some cases they can apply to values calculated via *$expression*. Constraints violations generate *ObjectTransformerException*.

| Example | Description |
| ------- | ----------- |
| "$constraints" : [{"$required":true}]           | A value is required. |
| "$constraints" : [{"$type":"number"}]           | A value must be a number. |
| "$constraints" : [{"$required":true}, {"$type":"number"}]    | A value is required and must be a number. |
| "$constraints" : [{"$values":[100, 200, 300]}]  | A numeric value must be 100 or 200 or 300. |
| "$constraints" : [{"$range":{"greater-than":10, "less-than":100.2}}]    | A numeric value must be between 10 and 100.2 |
| "$constraints" : [{"$range":{"greater-than":"abc", "less-than":"xyz"}}] | A string value must be in the lexicographic range. |

## More Examples

All examples are located in the unit tests folders and include a source JSON, a transform JSON, a target JSON (result of the transformation) and a Java unit test to run the example.

- [Arrays]
  (https://stash.pros.com/users/lsuardi/repos/json-transform/browse/src/test/java/com/transform/examples/arrays)
  => merge, filter, sort...
- [Constraints]
  (https://stash.pros.com/users/lsuardi/repos/json-transform/browse/src/test/java/com/transform/examples/constraints)
  => validate required, data type, values, range...
- [Expressions]
  (https://stash.pros.com/users/lsuardi/repos/json-transform/browse/src/test/java/com/transform/examples/expressions)
  => set, replace, append, array index, UUID...
- [Fields]
  (https://stash.pros.com/users/lsuardi/repos/json-transform/browse/src/test/java/com/transform/examples/fieldnames)
  => rename, restructure, copy...
- [Objects]
  (https://stash.pros.com/users/lsuardi/repos/json-transform/browse/src/test/java/com/transform/examples/objects)
  => reference, nested objects...
- [Paths]
  (https://stash.pros.com/users/lsuardi/repos/json-transform/browse/src/test/java/com/transform/examples/paths)
  => path vs. value, array index, array search, parent access...

## How To Use

```java
// need a Jackson ObjectMapper
ObjectMapper mapper = new ObjectMapper();

// ObjectTransformer takes an ObjectMapper
ObjectTransformer transformer = new ObjectTransformer(mapper);

// the transformation takes the source JSON and transform JSON strings 
// and returns the target JSON as a JsonNode
JsonNode result = transformer.transform(jsonSource, jsonTransform);
```

## Configuration

json-transform is configured using Java properties as follows:

```java
ObjectMapper mapper = new ObjectMapper();
Properties properties = new Properties();
properties.setProperty("exception.on.path.resolution", "true");
ObjectTransformer transformer = new ObjectTransformer(properties, mapper);
```

The configuration properties below are available:

| Property                      | Default | Description                                            |
| ----------------------------- | ------- | ------------------------------------------------------ |
| exception.on.path.resolution  | false   | When true an exception is thrown when a JSON path      |
|                               |         | cannot be resolved to a JSON object.                   |
| plugin.folder                 |   "."   | Where json-transform plugins are located.              |


## How To Build

Before importing the project in your favourite IDE, building the source with **gradle** is recommended to retrieve the necessary dependencies as well as verify integrity of the code (unit tests). From the project root folder at the command line:

- cd gradle
- gradlew build

The gradle build follows the standard gradle java plugin setup:

- *gradle/build/classes* contains the compiled source
- *gradle/build/libs* contains the json-transform.jar
- *\<user home\>/.gradle/caches/modules-2/files-2.1* contains the jar files json-transform depends on

## Plugin Development

json-transform can be extended by writing Java plugins for the various transform directives. Refer to the [json-transform-plugin] (https://stash.pros.com/users/lsuardi/repos/json-transform-plugin/browse) project for more information.
