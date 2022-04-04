# Nested Product Types

This example project shows the use of basic Product Types in combination of nested types.
It is intended to be an introduction into how this library is to be used.

The goal is to implement a type-safe version of this tutorial: https://docs.commercetools.com/tutorials/nested-types.
While the example focuses on Product Types, the same mechanics apply to Custom Fields as well.

## The Problem

Accessing attributes of Product Variants is not type-safe.
You have to know the attribute name as well as their types to use them correctly, and there is no IDE support for both.

## The Solution

Using type introspection, classes are generated and integrated into the official Java SDKv2.

### Introspection of Types

In order to generate classes, the library must have access to the commercetools types.
You can download them from a commercetools instance or provide the JSON files yourself.

For this example, you can find the Product Types here: [productTypes.json](src/main/resources/productTypes.json).

The first step is to apply the Gradle plugin and configure it to use the provided types.
It is necessary to provide a package name in which the generated classes are put into.

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

After configuring the plugin, running `./gradlew clean build` will generate the classes.
By default, generated classes are put into `build/generated/source/custom-types`.
Additionally, the generated classes are added to the source set and are automatically available within your Gradle project.

### A look at the generated code

The generated code relies heavily on Kotlin's delegation (1).
That ensures, that only the properties that are actively changed are overridden while everything else is delegated to the default implementation.

For each Product Type, 5 classes are generated:
- Product
- ProductCatalogData
- ProductData
- ProductVariant
- ProductVariantAttributes

They represent the product hierarchy required to type the variant attributes.
The Product Variant Attributes contain the attributes defined in the Product Type.

```kotlin
@JsonDeserialize(`as` = FoodTypeProduct::class)
class FoodTypeProduct @JsonCreator constructor(
  @JsonProperty("delegate")
  `delegate`: ProductImpl,
  @JsonProperty("masterData")
  private val masterData: FoodTypeProductCatalogData
) : Product by delegate, TypedProduct {
  override fun getMasterData(): FoodTypeProductCatalogData = this.masterData

  companion object {
    const val _TYPE_KEY: String = "food-type"
  }
}

@JsonDeserialize(`as` = FoodTypeProductCatalogData::class)
class FoodTypeProductCatalogData @JsonCreator constructor(
  @JsonProperty("delegate")
  `delegate`: ProductCatalogDataImpl,
  @JsonProperty("current")
  private val current: FoodTypeProductData,
  @JsonProperty("staged")
  private val staged: FoodTypeProductData
) : ProductCatalogData by delegate {
  override fun getCurrent(): FoodTypeProductData = this.current

  override fun getStaged(): FoodTypeProductData = this.staged
}

@JsonDeserialize(`as` = FoodTypeProductData::class)
class FoodTypeProductData @JsonCreator constructor(
  @JsonProperty("delegate")
  `delegate`: ProductDataImpl,
  @JsonProperty("masterVariant")
  private val masterVariant: FoodTypeProductVariant,
  @JsonProperty("variants")
  private val variants: List<FoodTypeProductVariant>
) : ProductData by delegate {
  override fun getMasterVariant(): FoodTypeProductVariant = this.masterVariant

  override fun getVariants(): List<FoodTypeProductVariant> = this.variants
}

@JsonDeserialize(`as` = FoodTypeProductVariant::class)
class FoodTypeProductVariant @JsonCreator constructor(
  @JsonProperty("delegate")
  `delegate`: ProductVariantImpl,
  @JsonProperty("typedAttributes")
  val typedAttributes: FoodTypeProductVariantAttributes
) : ProductVariant by delegate

@JsonDeserialize(`as` = FoodTypeProductVariantAttributes::class)
data class FoodTypeProductVariantAttributes(
  @JsonProperty("taste")
  val taste: String,
  @JsonProperty("nutrients")
  val nutrients: Set<NutrientInformationProductVariantAttributes>
) : TypedProductVariantAttributes {
  companion object {
    const val TASTE: String = "taste"

    const val NUTRIENTS: String = "nutrients"
  }
}
```

Please note the type of the property `nutrients`. It's the nested Product Type. 
The annotations configure Jackson to delegate deserialization and narrow types.

### Extending the commercetools SDK

In addition to the above-mentioned classes, a Jackson Api Module is generated.
This module has to be registered in order to switch out the default implementation.

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

After that instead of `ProductImpl` instances, you will find instances of the generated classes.
The deserializer will inspect the Product Type Identifier at runtime to figure out the appropriate type.

To see everything in action, call `./gradlew -q run`.

_______________
(1) https://kotlinlang.org/docs/delegation.html