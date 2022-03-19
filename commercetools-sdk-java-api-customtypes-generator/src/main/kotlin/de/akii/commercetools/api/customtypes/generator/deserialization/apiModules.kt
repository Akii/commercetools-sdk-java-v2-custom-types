package de.akii.commercetools.api.customtypes.generator.deserialization

import com.commercetools.api.models.product.Product
import com.commercetools.api.models.product.ProductImpl
import com.commercetools.api.models.product_type.ProductType
import com.commercetools.api.models.type.Type
import com.fasterxml.jackson.databind.module.SimpleModule
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import de.akii.commercetools.api.customtypes.generator.common.*
import de.akii.commercetools.api.customtypes.generator.model.TypedResourceFile
import io.vrap.rmf.base.client.utils.Generated

fun apiModulesFile(typedResourceFiles: List<TypedResourceFile>, config: Configuration): FileSpec {
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
            .builder("resolveTypeName")
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

private fun typedCustomFieldsApiModule(typedResourceFiles: List<TypedResourceFile>, config: Configuration): TypeSpec =
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
            config
                .customTypes
                .flatMap { it.resourceTypeIds }
                .toSet()
                .forEach {
                    add(
                        "addDeserializer(%1T::class.java, %2T(typeResolver))\n",
                        resourceTypeIdToClassName(it, config),
                        TypedCustomFieldsDeserializer(it, config).className
                    )
                }

            typedResourceFiles.forEach {
                add(
                    "addDeserializer(%1T::class.java, %2T(%3T::class))\n",
                    it.resourceInterface,
                    TypedResourceDeserializer(config).className,
                    it.typedResourceClassName
                )
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
        .addAnnotation(deserializeAs(Product::class.asClassName()))
        .build()

private fun fallbackProductInterface(config: Configuration) =
    TypeSpec
        .interfaceBuilder(ClassName(config.packageName, "FallbackProduct"))
        .addAnnotation(Generated::class)
        .addAnnotation(deserializeAs(ProductImpl::class.asClassName()))
        .build()

private fun typedResourceInterface(typedResourceFile: TypedResourceFile, config: Configuration) =
    TypeSpec
        .interfaceBuilder(ClassName(config.packageName, "${typedResourceFile.typedResourceClassName.simpleName}Resource"))
        .addAnnotation(Generated::class)
        .addAnnotation(deserializeAs(typedResourceFile.resourceInterface.asClassName()))
        .build()

private fun fallbackResourceInterface(typedResourceFile: TypedResourceFile, config: Configuration) =
    TypeSpec
        .interfaceBuilder(ClassName(config.packageName, "Fallback${typedResourceFile.typedResourceClassName.simpleName}Resource"))
        .addAnnotation(Generated::class)
        .addAnnotation(deserializeAs(typedResourceFile.resourceDefaultImplementation.asClassName()))
        .build()
