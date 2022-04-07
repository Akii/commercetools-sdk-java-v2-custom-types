# Custom Fields

This example project shows the use of typed Custom Fields.

The goal is to implement a type-safe version of this tutorial: https://docs.commercetools.com/tutorials/custom-types

## The Problem

Accessing Custom Fields with the provided tools like `CustomFieldsAccessor` is not type-safe. It has no IDE support and
makes your code harder to refactor.

After completing the above tutorial you'll end up with a JSON structure like this:

```json
{
  "id": "85c8d7a1-da11-477f-8ad7-a19c6c274f71",
  "...": "...",
  "custom": {
    "type": {
      "typeId": "type",
      "id": "b9c5e724-8d86-485b-ae1e-a1d3bcc8deae"
    },
    "fields": {
      "preferredShoeSize": {
        "en": "38"
      }
    }
  }
}
```

Accessing this structure with the provided tools looks like this:

```kotlin
val fieldAccessor = CustomFieldsAccessor(customer.custom)

println(fieldAccessor.asLocalizedString("preferredShoeSize")!!.get("en"))
```

Note how you need to know the field name as well as the type in advance.

## The Solution

Using this library, typed Custom Field classes are generated and integrated into the official Java SDKv2.

### Generation of typed Custom Field classes

Like with Product Types, the library must be supplied with the types of Custom Fields.

For this example, you can find the types here: [types.json](src/main/resources/types.json).

The first step is to apply the Gradle plugin and configure it to use the provided types. It is necessary to provide a
package name in which the generated classes are put into.

```kotlin
import de.akii.commercetools.api.customtypes.plugin.gradle.commercetoolsCustomTypes

plugins {
    id("de.akii.commercetools.api.customtypes") version "0.0.21"
}

commercetoolsCustomTypes {
    packageName = "org.example.models"

    customFields {
        typesFile = File("./src/main/resources/types.json")
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
        .registerModule(CustomFieldsApiModule())

val apiRoot: ProjectApiRoot =
    ApiRootBuilder.of()
        // …
        .withSerializer(ResponseSerializer.of(objectMapper))
        .build("…")
```

After that instead of default implementations, you will find instances of the generated classes. The deserializer will
inspect the Custom Type Identifier at runtime to figure out the appropriate type.

### Using typed Custom Fields

The generated classes will give you access to typed fields. Now, instead of guessing, you know exactly which types
to expect.

```kotlin
println(customer.custom.typedFields.preferredShoeSize.get("en"))
```

Notice how the field name and type are now known at compile time.

To see everything in action, call `./gradlew -q run`.