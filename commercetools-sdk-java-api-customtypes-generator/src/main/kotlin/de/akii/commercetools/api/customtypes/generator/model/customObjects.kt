package de.akii.commercetools.api.customtypes.generator.model

import com.commercetools.api.models.custom_object.CustomObject
import com.commercetools.api.models.custom_object.CustomObjectImpl
import com.commercetools.api.models.product.ProductCatalogDataImpl
import com.squareup.kotlinpoet.*
import de.akii.commercetools.api.customtypes.generator.common.*

fun typedCustomObjectInterface(config: Configuration): TypeSpec =
    TypeSpec
        .interfaceBuilder(TypedCustomObjectInterface(config).className)
        .addAnnotation(generated)
        .build()

fun typedCustomObject(containerName: String, className: String, config: Configuration): Pair<ClassName, TypeSpec> {
    val typedCustomObjectClassName = TypedCustomObject(containerName, className, config).className
    val valueClassName = TypedCustomObjectValue(className)

    return typedCustomObjectClassName to TypeSpec
        .classBuilder(typedCustomObjectClassName)
        .addAnnotation(generated)
        .addAnnotation(deserializeAs(typedCustomObjectClassName))
        .addModifiers(KModifier.DATA)
        .primaryConstructor(
            FunSpec
                .constructorBuilder()
                .addAnnotation(jsonCreator)
                .addParameter(
                    ParameterSpec
                        .builder("delegate", CustomObjectImpl::class)
                        .addAnnotation(jsonProperty("delegate"))
                        .build()
                )
                .addParameter(
                    ParameterSpec
                        .builder("value", valueClassName.className)
                        .addAnnotation(jsonProperty("value"))
                        .build()
                )
                .build()
        )
        .addSuperinterface(CustomObject::class, "delegate")
        .addSuperinterface(TypedCustomObjectInterface(config).className)
        .addProperty(
            PropertySpec
                .builder("delegate", CustomObjectImpl::class)
                .addModifiers(KModifier.PRIVATE)
                .initializer("delegate")
                .build()
        )
        .addProperty(
            PropertySpec
                .builder("value", valueClassName.className)
                .initializer("value")
                .addModifiers(KModifier.PRIVATE)
                .build()
        )
        .addFunction(
            FunSpec
                .builder("getValue")
                .returns(valueClassName.className)
                .addStatement("return this.value")
                .addModifiers(KModifier.OVERRIDE)
                .build()
        )
        .addType(TypeSpec
            .companionObjectBuilder()
            .addProperty(PropertySpec
                .builder("CONTAINER_NAME", String::class)
                .addModifiers(KModifier.CONST)
                .initializer("%S", containerName)
                .build()
            )
            .build()
        )
        .build()
}