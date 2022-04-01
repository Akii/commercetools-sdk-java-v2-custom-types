# Nested Product Types

This example project shows the use of basic Product Types in combination of nested types.

It is the type-safe implementation of this tutorial: https://docs.commercetools.com/tutorials/nested-types.
The same concept applies to Custom Fields.

## The Problem

Accessing attributes of Product Variants is not type-safe.

## The Solution

This library expresses commercetools types as Java/Kotlin types and integrate them into the official Java SDKv2.

### Introspection of Types

In order to generate classes, the library must have access to the commercetools types.
You can download them from a commercetools instance or provide the JSON files yourself.

