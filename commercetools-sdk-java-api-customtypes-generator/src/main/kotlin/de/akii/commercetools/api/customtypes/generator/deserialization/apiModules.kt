package de.akii.commercetools.api.customtypes.generator.deserialization

import com.commercetools.api.models.product.Product
import com.commercetools.api.models.product.ProductImpl
import com.commercetools.api.models.product_type.ProductType
import com.commercetools.api.models.type.Type
import com.fasterxml.jackson.databind.module.SimpleModule
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import de.akii.commercetools.api.customtypes.generator.common.*
import de.akii.commercetools.api.customtypes.generator.model.TypedResources
import io.vrap.rmf.base.client.utils.Generated

fun apiModulesFile(typedResourceFiles: List<TypedResources>, config: Configuration): FileSpec {
    val file = FileSpec
        .builder(config.packageName, "apiModules")
        .addType(typeResolver(config))

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

private fun typeResolver(config: Configuration) =
    TypeSpec
        .interfaceBuilder(TypeResolver(config).className)
        .addTypeVariable(TypeVariableName.invoke("A"))
        .addFunction(FunSpec
            .builder("resolveTypeKey")
            .addModifiers(KModifier.ABSTRACT)
            .addParameter("typeId", String::class)
            .returns(String::class.asTypeName().copy(nullable = true))
            .build()
        )
        .build()

private fun customProductApiModule(config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder(ClassName(config.packageName, "CustomProductApiModule"))
        .addAnnotation(Generated::class)
        .superclass(SimpleModule::class)
        .primaryConstructor(FunSpec
            .constructorBuilder()
            .addParameter(ParameterSpec
                .builder("typeResolver", TypeResolver(config).className.parameterizedBy(ProductType::class.asTypeName()))
                .defaultValue("%T()", ProductTypeResolver(config).className)
                .build()
            )
            .build()
        )
        .addInitializerBlock(buildCodeBlock {
            add(
                "addDeserializer(%1T::class.java, %2T(typeResolver))\n",
                Product::class,
                CustomProductDeserializer(config).className
            )
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
                CustomProductVariantAttributesModifier(config).className,
            )
        })
        .build()

private fun typedCustomFieldsApiModule(typedResourceFiles: List<TypedResources>, config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder(ClassName(config.packageName, "TypedCustomFieldsApiModule"))
        .addAnnotation(Generated::class)
        .superclass(SimpleModule::class)
        .primaryConstructor(FunSpec
            .constructorBuilder()
            .addParameter(ParameterSpec
                .builder("typeResolver", TypeResolver(config).className.parameterizedBy(Type::class.asTypeName()))
                .defaultValue("%T()", CustomTypeResolver(config).className)
                .build()
            )
            .build()
        )
        .addInitializerBlock(buildCodeBlock {
            typedResourceFiles.forEach {
                add(
                    "addDeserializer(%1T::class.java, %2T(typeResolver))\n",
                    it.resourceInterface,
                    TypedResourceDeserializer(it).className
                )
                add(
                    "setMixInAnnotation(%1T::class.java, %2L::class.java)\n",
                    it.resourceInterface,
                    "Custom${it.resourceInterface.simpleName}"
                )
                add(
                    "setMixInAnnotation(%1T::class.java, %2L::class.java)\n",
                    it.resourceDefaultImplementation,
                    "Fallback${it.resourceInterface.simpleName}"
                )
            }
        })
        .build()

private fun customProductInterface(config: Configuration): TypeSpec =
    TypeSpec
        .interfaceBuilder(ClassName(config.packageName, "CustomProduct"))
        .addAnnotation(Generated::class)
        .addAnnotation(deserializeAs(Product::class.asClassName()))
        .build()

private fun fallbackProductInterface(config: Configuration) =
    TypeSpec
        .interfaceBuilder(ClassName(config.packageName, "FallbackProduct"))
        .addAnnotation(Generated::class)
        .addAnnotation(deserializeAs(ProductImpl::class.asClassName()))
        .build()

private fun typedResourceInterface(typedResourceFile: TypedResources, config: Configuration) =
    TypeSpec
        .interfaceBuilder(ClassName(config.packageName, "Custom${typedResourceFile.resourceInterface.simpleName}"))
        .addAnnotation(Generated::class)
        .addAnnotation(deserializeAs(typedResourceFile.resourceInterface.asClassName()))
        .build()

private fun fallbackResourceInterface(typedResourceFile: TypedResources, config: Configuration) =
    TypeSpec
        .interfaceBuilder(ClassName(config.packageName, "Fallback${typedResourceFile.resourceInterface.simpleName}"))
        .addAnnotation(Generated::class)
        .addAnnotation(deserializeAs(typedResourceFile.resourceDefaultImplementation.asClassName()))
        .build()
