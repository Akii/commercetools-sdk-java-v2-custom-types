# commercetools Java SDK custom types

This library extends the official commercetools Java SDK by generating custom types defined in your commercetools project.
Currently, type-safe product types are supported. The goal is to support type-safe reference expansion, custom fields and even custom objects.

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

plugins {
    id("de.akii.commercetools.api.customtypes") version $pluginVersion
}

commercetoolsCustomTypes {
    clientId = "<client-id>"
    clientSecret = "<client-secret>"
    serviceRegion = "GCP_EUROPE_WEST1"
    projectName = "<project-name>"
    packageName = "your.types.go.here"
}
```

Alternatively, you can provide the product types yourself.
To do so, do not specify client credentials but instead, configure the path to the product types JSON file.

```kotlin
import de.akii.commercetools.api.customtypes.plugin.gradle.commercetoolsCustomTypes

plugins {
    id("de.akii.commercetools.api.customtypes") version $pluginVersion
}

commercetoolsCustomTypes {
    productTypesFile = File("./productTypes.json")
    packageName = "your.types.go.here"
}
```

The plugin will now automatically generate custom product types based on your product type definition.

### commercetools SDK

Once you've generated your custom types, you can configure the official commercetools SDK API to use them.
To do so, you need to register the generated Jackson module `CustomProductApiModule`.

```kotlin
import com.commercetools.api.defaultconfig.ApiRootBuilder
import com.commercetools.api.defaultconfig.ServiceRegion
import io.vrap.rmf.base.client.ResponseSerializer
import io.vrap.rmf.base.client.oauth2.ClientCredentials
import io.vrap.rmf.base.client.utils.json.JsonUtils
import your.types.go.here.CustomProductApiModule

val objectMapper =
    JsonUtils
        .createObjectMapper()
        .registerModule(CustomProductApiModule())

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

## Type-safe product attributes

TODO(Document behaviour)

## Contributing

To get started, please fork the repo and checkout a new branch. You can then build the library locally with Gradle.

```shell script
./gradlew clean build
```

After you have your local branch set up, take a look at our [open issues](https://github.com/Akii/commercetools-sdk-java-v2-custom-types/issues) to see where you can contribute.

## License

This library is licensed under the [MIT](LICENSE) license.