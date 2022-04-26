package de.akii.commercetools.api.customtypes.generator.model.product

import com.commercetools.api.models.product.*
import com.squareup.kotlinpoet.*
import de.akii.commercetools.api.customtypes.generator.common.*
import de.akii.commercetools.api.customtypes.generator.common.TypedProductVariant

fun typedProductVariantBuilderExtensionFunctions(
    typedProductVariantClassName: TypedProductVariant,
    typedProductVariantAttributesClassName: TypedProductVariantAttributes
): Pair<FunSpec, FunSpec> {
    val build = FunSpec
        .builder("build${typedProductVariantClassName.className.simpleName}")
        .receiver(ProductVariantBuilder::class)
        .addParameter("typedAttributes", typedProductVariantAttributesClassName.className)
        .addCode(
            "return %1T(this.build() as %2T, typedAttributes)",
            typedProductVariantClassName.className,
            ProductVariantImpl::class
        )
        .returns(typedProductVariantClassName.className)
        .build()

    val buildUnchecked = FunSpec
        .builder("build${typedProductVariantClassName.className.simpleName}Unchecked")
        .receiver(ProductVariantBuilder::class)
        .addParameter("typedAttributes", typedProductVariantAttributesClassName.className)
        .addCode(
            "return %1T(this.buildUnchecked() as %2T, typedAttributes)",
            typedProductVariantClassName.className,
            ProductVariantImpl::class
        )
        .returns(typedProductVariantClassName.className)
        .build()

    return build to buildUnchecked
}

fun productVariant(
    typedProductVariantClassName: TypedProductVariant,
    typedProductVariantAttributesClassName: TypedProductVariantAttributes
): TypeSpec = TypeSpec
    .classBuilder(typedProductVariantClassName.className)
    .addAnnotation(generated)
    .addAnnotation(deserializeAs(typedProductVariantClassName.className))
    .addModifiers(KModifier.DATA)
    .primaryConstructor(FunSpec
        .constructorBuilder()
        .addAnnotation(jsonCreator)
        .addParameter(ParameterSpec
            .builder("delegate", ProductVariantImpl::class)
            .addAnnotation(jsonProperty("delegate"))
            .build()
        )
        .addParameter(ParameterSpec
            .builder("typedAttributes", typedProductVariantAttributesClassName.className)
            .addAnnotation(jsonProperty("typedAttributes"))
            .build()
        )
        .build()
    )
    .addSuperinterface(com.commercetools.api.models.product.ProductVariant::class, "delegate")
    .addProperty(
        PropertySpec
            .builder("delegate", ProductVariantImpl::class)
            .addModifiers(KModifier.PRIVATE)
            .initializer("delegate")
            .build()
    )
    .addProperty(
        PropertySpec
            .builder("typedAttributes", typedProductVariantAttributesClassName.className)
            .mutable()
            .initializer("typedAttributes")
            .build()
    )
    .build()