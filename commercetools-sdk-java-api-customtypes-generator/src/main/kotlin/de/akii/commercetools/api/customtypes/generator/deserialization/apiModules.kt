package de.akii.commercetools.api.customtypes.generator.deserialization

import com.commercetools.api.models.product.Product
import com.commercetools.api.models.product.ProductImpl
import com.commercetools.api.models.type.CustomFields
import com.commercetools.api.models.type.CustomFieldsImpl
import com.fasterxml.jackson.databind.module.SimpleModule
import com.squareup.kotlinpoet.*
import de.akii.commercetools.api.customtypes.generator.TypedResourceFile
import de.akii.commercetools.api.customtypes.generator.common.*
import io.vrap.rmf.base.client.utils.Generated

fun apiModulesFile(typedResourceFiles: List<TypedResourceFile>, config: Configuration): FileSpec {
    val file = FileSpec
        .builder(config.packageName, "apiModules")
        .addType(typedCustomFieldsInterface(config))
        .addType(fallbackCustomFieldsInterface(config))

    if (config.productTypes.isNotEmpty()) {
        file
            .addType(customProductInterface(config))
            .addType(fallbackProductInterface(config))
            .addType(customProductApiModule(config))
    }

    if (config.customTypes.isNotEmpty()) {
        typedResourceFiles.forEach {
            file.addType(typedResourceInterface(it, config))
            file.addType(fallbackResourceInterface(it, config))
        }

        file.addType(typedCustomFieldsApiModule(typedResourceFiles, config))
    }

    return file.build()
}

private fun customProductApiModule(config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder(ClassName(config.packageName, "CustomProductApiModule"))
        .addAnnotation(Generated::class)
        .superclass(SimpleModule::class)
        .addInitializerBlock(buildCodeBlock {
            add(
                "setMixInAnnotation(%1T::class.java, %2L::class.java)\n",
                Product::class,
                "CustomProduct"
            )
            add(
                "setMixInAnnotation(%1T::class.java, %2L::class.java)\n",
                ProductImpl::class,
                "FallbackProduct"
            )
            add(
                "setDeserializerModifier(%1T())\n",
                CustomProductVariantAttributesModifierClassName(config).className,
            )
        })
        .build()

private fun typedCustomFieldsApiModule(typedResourceFiles: List<TypedResourceFile>, config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder(ClassName(config.packageName, "TypedCustomFieldsApiModule"))
        .addAnnotation(Generated::class)
        .superclass(SimpleModule::class)
        .addInitializerBlock(buildCodeBlock {
            add(
                "setMixInAnnotation(%1T::class.java, %2L::class.java)\n",
                CustomFields::class,
                "TypedCustomFields"
            )
            add(
                "setMixInAnnotation(%1T::class.java, %2L::class.java)\n",
                CustomFieldsImpl::class,
                "FallbackCustomFields"
            )

            typedResourceFiles.forEach {
                add(
                    "setMixInAnnotation(%1T::class.java, %2L::class.java)\n",
                    it.resourceInterface,
                    "${it.typedResourceClassName.simpleName}Resource"
                )
                add(
                    "setMixInAnnotation(%1T::class.java, %2L::class.java)\n",
                    it.resourceDefaultImplementation,
                    "Fallback${it.typedResourceClassName.simpleName}Resource"
                )
            }
        })
        .build()

private fun customProductInterface(config: Configuration): TypeSpec =
    TypeSpec
        .interfaceBuilder(ClassName(config.packageName, "CustomProduct"))
        .addAnnotation(Generated::class)
        .addAnnotation(deserializeUsing(CustomProductDeserializerClassName(config).className))
        .build()

private fun fallbackProductInterface(config: Configuration) =
    TypeSpec
        .interfaceBuilder(ClassName(config.packageName, "FallbackProduct"))
        .addAnnotation(Generated::class)
        .addAnnotation(deserializeAs(ProductImpl::class.asClassName()))
        .build()

fun customProductVariantAttributesInterface(config: Configuration) =
    TypeSpec
        .interfaceBuilder(CustomProductVariantAttributesClassName(config).className)
        .addAnnotation(Generated::class)
        .build()

private fun typedCustomFieldsInterface(config: Configuration): TypeSpec =
    TypeSpec
        .interfaceBuilder(ClassName(config.packageName, "TypedCustomFields"))
        .addAnnotation(Generated::class)
        .addAnnotation(deserializeUsing(TypedCustomFieldsDeserializerClassName(config).className))
        .build()

private fun fallbackCustomFieldsInterface(config: Configuration) =
    TypeSpec
        .interfaceBuilder(ClassName(config.packageName, "FallbackCustomFields"))
        .addAnnotation(Generated::class)
        .addAnnotation(deserializeAs(CustomFieldsImpl::class.asClassName()))
        .build()

private fun typedResourceInterface(typedResourceFile: TypedResourceFile, config: Configuration) =
    TypeSpec
        .interfaceBuilder(ClassName(config.packageName, "${typedResourceFile.typedResourceClassName.simpleName}Resource"))
        .addAnnotation(Generated::class)
        .addAnnotation(deserializeUsing(TypedResourceDeserializerClassName(typedResourceFile, config).className))
        .build()

private fun fallbackResourceInterface(typedResourceFile: TypedResourceFile, config: Configuration) =
    TypeSpec
        .interfaceBuilder(ClassName(config.packageName, "Fallback${typedResourceFile.typedResourceClassName.simpleName}Resource"))
        .addAnnotation(Generated::class)
        .addAnnotation(deserializeAs(typedResourceFile.resourceDefaultImplementation.asClassName()))
        .build()
