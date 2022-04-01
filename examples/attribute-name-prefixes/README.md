# Attribute Name Prefixes

This example project shows the use of attribute name prefixes to get around limitations of the commercetools platform.

## The Problem

You may have come across the restriction that every attribute / field name is tied to a type across all Product Types / Custom Fields.

> When using the same name for an attribute in two or more Product Types all fields of the AttributeDefinition of this attribute need to be the same across the Product Types.

Because of that restrictions, the following two Product Types cannot exist at the same time:

```json
{
  "key": "type-a",
  "attributes": [
    {
      "name": "property-name",
      "type": {
        "name": "number"
      }
    }
  ]
}
```

```json
{
  "key": "type-b",
  "attributes": [
    {
      "name": "property-name",
      "type": {
        "name": "text"
      }
    }
  ]
}
```

When trying to create the above types, the platform will return a `AttributeDefinitionAlreadyExists` error, as described in the documentation (1).

## The Solution

To circumvent that limitation, you can leverage the library's capabilities of attribute to property name translation.
All concepts shown here apply to field names of Custom Fields as well.

The idea is to prefix attribute names with the Product Type key and configure property name generation to strip that prefix.

### Prefix Attribute Names of your Product Types

The first step is to prefix attribute names. Whereas the Product Types above cannot be created, the following types with prefixed attribute names can.

```json
{
  "key": "type-a",
  "attributes": [
    {
      "name": "type-a_property-name",
      "type": {
        "name": "number"
      }
    }
  ]
}
```

```json
{
  "key": "type-b",
  "attributes": [
    {
      "name": "type-b_property-name",
      "type": {
        "name": "text"
      }
    }
  ]
}
```

### Configure Attribute to Property Name generation

Of course, the goal is to have the same property name on our generated typed Product classes.
To achieve that, we need to configure code generation to strip the prefix of the attribute name.

```kotlin
// build.gradle.kts
commercetoolsCustomTypes {
    productTypes {
        attributeToPropertyName = { productType, attribute ->
            attribute
                .name
                .replace("${productType.key}_", "") // turn "type-(a|b)_property-name" into "property-name"
                .split('-', '_')
                .joinToString("") { part ->
                    part.replaceFirstChar { it.toString().toUpperCase() }
                }
                .replaceFirstChar { it.toString().toLowerCase() }
        }
    }
}
```

Using this configuration, we now have two typed Product Variant Attributes.
Notice how the property names are the same yet the type differs.

```kotlin
data class TypeAProductVariantAttributes(
  @JsonProperty("type-a_property-name")
  val propertyName: Double
) : TypedProductVariantAttributes {
  companion object {
    const val PROPERTY_NAME: String = "type-a_property-name"
  }
}
```

```kotlin
data class TypeBProductVariantAttributes(
  @JsonProperty("type-b_property-name")
  val propertyName: String
) : TypedProductVariantAttributes {
  companion object {
    const val PROPERTY_NAME: String = "type-b_property-name"
  }
}
```

Under the hood they are mapped from the prefixed attribute names.
Using the constant `PROPERTY_NAME` you can create Update Actions to update attribute values.

____________
(1) https://docs.commercetools.com/api/projects/productTypes#attributedefinition