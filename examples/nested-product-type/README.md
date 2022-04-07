# Nested Product Types

This example project shows the use of typed Product Types in combination of nesting thereof. It is intended to be an
introduction into how this library is to be used.

The goal is to implement a type-safe version of this tutorial: https://docs.commercetools.com/tutorials/nested-types.

## The Problem

Accessing product attributes with the provided tools like `AttributeAccessor` is not type-safe. It has no IDE support
and makes your code harder to refactor.

After completing the above tutorial you'll end up with a JSON structure like this:

```json
{
  "id": "0bdef965-9508-47ec-bf7f-3e2c41e67fb2",
  "key": "nutrient-information",
  "...": "...",
  "masterData": {
    "current": {
      "masterVariant": {
        "attributes": [
          {
            "name": "taste",
            "value": "excellent!"
          },
          {
            "name": "nutrients",
            "value": [
              [
                {
                  "name": "quantityContained",
                  "value": 1.4
                },
                {
                  "name": "nutrientTypeCode",
                  "value": "FAT"
                }
              ]
            ]
          }
        ]
      }
    }
  }
}
```

Accessing this structure with the provided tools might very well look like this:

```kotlin
val taste = productVariant.withProductVariant(AttributesAccessor::of).asString("taste")
val nutrients = productVariant.withProductVariant(AttributesAccessor::of).get("nutrients")
val nutrientsValues = nutrients!!.value as List<*>

println("The taste is $taste for these nutrients:")
nutrientsValues.forEach { it as ArrayList<AttributeImpl>
    println("   Code: ${it[1].value} and Quality: ${it[0].value}")
}
```

## The Solution

Using this library, typed product classes are generated and integrated into the official Java SDKv2.

### Generation of typed product classes

In order to generate typed product classes, the library must have access to the commercetools types. You can download them from a
commercetools instance or provide the JSON files yourself.

For this example, you can find the Product Types here: [productTypes.json](src/main/resources/productTypes.json).

The first step is to apply the Gradle plugin and configure it to use the provided types. It is necessary to provide a
package name in which the generated classes are put into.

```kotlin
import de.akii.commercetools.api.customtypes.plugin.gradle.commercetoolsCustomTypes

plugins {
    id("de.akii.commercetools.api.customtypes") version "0.0.21"
}

commercetoolsCustomTypes {
    packageName = "org.example.models"

    productTypes {
        productTypesFile = File("./src/main/resources/productTypes.json")
    }
}
```

After configuring the plugin, running `./gradlew clean build` will generate the classes. By default, generated classes
are put into `build/generated/source/custom-types`. Additionally, the generated classes are added to the source set and
are automatically available within your Gradle project.

### Extending the commercetools SDK

In addition to the typed product classes, a Jackson Api Module is generated. This module has to be registered in order
configure Jackson to use the generated classes.

```kotlin
val objectMapper: ObjectMapper =
    JsonUtils
        .createObjectMapper()
        .registerModule(TypedProductApiModule())

val apiRoot: ProjectApiRoot =
    ApiRootBuilder.of()
        // …
        .withSerializer(ResponseSerializer.of(objectMapper))
        .build("…")
```

After that instead of `ProductImpl` instances, you will find instances of the generated classes. The deserializer will
inspect the Product Type Identifier at runtime to figure out the appropriate type.

### Using typed product attributes

The generated classes will give you access to typed attributes. Now, instead of guessing, you know exactly which types to expect.

```kotlin
val typedAttributes = productVariant.typedAttributes

println("The taste is ${typedAttributes.taste} for these nutrients:")
typedAttributes.nutrients.forEach { nutrient ->
    println("   Code: ${nutrient.nutrientTypeCode} and Quantity: ${nutrient.quantityContained}")
}
```

To see everything in action, call `./gradlew -q run`.