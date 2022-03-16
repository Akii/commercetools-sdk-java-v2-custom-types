# commercetools Java SDK custom types

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.akii.commercetools/commercetools-sdk-java-api-customtypes-generator/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.akii.commercetools/commercetools-sdk-java-api-customtypes-generator)

This library extends the official commercetools Java SDK by generating custom types defined in commercetools projects.
Currently, type-safe product types, reference expansion and custom fields are supported.

## Why?

Accessing product attributes and custom fields with the provided tools like `AttributeAccessor` is not type-safe.
It has no IDE support and makes your code harder to refactor. This library aims to provide types for all your custom commercetools types.

Given a product-type like this:

```json
{
  "id": "e8de347b-38fa-401d-a996-aa118658a90f",
  "name": "test",
  "attributes": [
    {
      "name": "a-boolean",
      "type": {
        "name": "boolean"
      }
    },
    {
      "name": "an-enum",
      "type": {
        "name": "enum",
        "values": []
      }
    },
    {
      "name": "ref-set",
      "type": {
        "name": "set",
        "elementType": {
          "name": "reference",
          "referenceTypeId": "product"
        }
      }
    },
    {
      "name": "nested-second-type",
      "type": {
        "name": "nested",
        "typeReference": {
          "typeId": "product-type",
          "id": "30313b5a-8573-4d3e-bfbf-566238168505"
        }
      }
    }
  ]
}
```

the library will generate the following classes (simplified):

```kotlin
class TestProduct : Product
class TestProductCatalogData : ProductCatalogData
class TestProductData : ProductData
class TestProductVariant : ProductVariant

data class TestProductVariantAttributes (
    val aBoolean: Boolean?,
    val anEnum: AttributePlainEnumValue?,
    val refSet: Set<ProductReference>?,
    val nestedSecondType: SecondTypeProductVariantAttributes?
)
```

Instead of dealing with attributes like this:

```kotlin
fun fetchProduct(productId: String): Product

val productVariant =
    fetchProduct("some-id")
        .masterData
        .current
        .masterVariant

// as of v8 beta 1
print(productVariant.withProductVariant(AttributesAccessor::of).asBoolean("a-boolean"))
```

you can now use typed attributes:

```kotlin
fun fetchProduct(productId: String): Product

val productVariant =
    fetchProduct("some-id")
        .masterData
        .current
        .masterVariant

when (productVariant) {
    is TestProductVariant -> print(productVariant.typedAttributes.aBoolean)
    else -> TODO()
}
```

Since the library generates classes conforming to all API interfaces, you can start using it without the need to refactor all your existing code.

## Modules

* [generator](/commercetools-sdk-java-api-customtypes-generator) - Code for generating type-safe custom types defined in commercetools projects
* [gradle-plugin](/commercetools-sdk-java-api-customtypes-gradle-plugin) - Gradle Plugin that generates type-safe custom types

## Usage

While the package `generator` is published as a stand-alone library, the most common use case is generating custom types by using the Gradle Plugin.

### Gradle Plugin

The quickest way to generate custom types for your commercetools project is to simply download them.
To do so, generate an API client with the scopes `view_types` and `view_products`, and then configure the plugin like this:

```kotlin
import de.akii.commercetools.api.customtypes.plugin.gradle.commercetoolsCustomTypes
import com.commercetools.api.defaultconfig.ServiceRegion

plugins {
    id("de.akii.commercetools.api.customtypes") version $pluginVersion
}

commercetoolsCustomTypes {
    packageName = "your.types.go.here"
    
    credentials {
        clientId = "<client-id>"
        clientSecret = "<client-secret>"
        serviceRegion = ServiceRegion.GCP_EUROPE_WEST1
        projectName = "<project-name>"
    }
}
```

Alternatively, you can provide the types yourself.
To do so, configure paths to the JSON files instead.

```kotlin
import de.akii.commercetools.api.customtypes.plugin.gradle.commercetoolsCustomTypes

plugins {
    id("de.akii.commercetools.api.customtypes") version $pluginVersion
}

commercetoolsCustomTypes {
    packageName = "your.types.go.here"
    
    productTypes {
        productTypesFile = File("./productTypes.json")
    }

    customFields {
        typesFile = File("./types.json")
    }
}
```

The plugin will now automatically generate custom types based on your type definitions.

### commercetools SDK

Once you've generated your custom types, you can configure the official commercetools SDK API to use them.
To do so, you need to register the generated Jackson module `CustomProductApiModule` and/or `TypedCustomFieldsApiModule`.

```kotlin
import com.commercetools.api.defaultconfig.ApiRootBuilder
import com.commercetools.api.defaultconfig.ServiceRegion
import io.vrap.rmf.base.client.ResponseSerializer
import io.vrap.rmf.base.client.oauth2.ClientCredentials
import io.vrap.rmf.base.client.utils.json.JsonUtils
import your.types.go.here.CustomProductApiModule
import your.types.go.here.TypedCustomFieldsApiModule

val objectMapper =
    JsonUtils
        .createObjectMapper()
        .registerModule(CustomProductApiModule())
        .registerModule(TypedCustomFieldsApiModule())

val ctApi =
    ApiRootBuilder.of()
        .defaultClient(
            ClientCredentials.of()
                .withClientId("<client-id>")
                .withClientSecret("<client-secret>")
                .build(),
            ServiceRegion.GCP_EUROPE_WEST1
        )
        .withSerializer(ResponseSerializer.of(objectMapper))
        .build("<project-name>")
```

For alternative ways of configuring the SDK, please consult the [commercetools documentation](https://commercetools.github.io/commercetools-sdk-java-v2/javadoc/com/commercetools/docs/meta/Serialization.html) on client customization.
This library introduces no breaking changes to the API.

## Contributing

To get started, please fork the repo and checkout a new branch. You can then build the library locally with Gradle.

```shell script
./gradlew clean build
```

After you have your local branch set up, take a look at our [open issues](https://github.com/Akii/commercetools-sdk-java-v2-custom-types/issues) to see where you can contribute.

## License

This library is licensed under the [MIT](LICENSE) license.