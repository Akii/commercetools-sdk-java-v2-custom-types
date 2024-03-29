package de.akii.commercetools.api.customtypes.generator.deserialization

import com.commercetools.api.models.custom_object.CustomObject
import com.commercetools.api.models.custom_object.CustomObjectImpl
import com.commercetools.api.models.order.ReturnItem
import com.commercetools.api.models.order.ReturnItemImpl
import com.commercetools.api.models.product.Product
import com.commercetools.api.models.product.ProductImpl
import com.commercetools.api.models.product.ProductProjection
import com.commercetools.api.models.product.ProductProjectionImpl
import com.commercetools.api.models.product_type.ProductType
import com.commercetools.api.models.type.Type
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id
import com.fasterxml.jackson.databind.module.SimpleModule
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import de.akii.commercetools.api.customtypes.generator.common.*
import de.akii.commercetools.api.customtypes.generator.model.TypedResources

fun typeResolverInterface(config: Configuration) =
    TypeSpec
        .interfaceBuilder(TypeResolver(config).className)
        .addAnnotation(generated)
        .addTypeVariable(TypeVariableName.invoke("A"))
        .addFunction(FunSpec
            .builder("resolveTypeKeyById")
            .addModifiers(KModifier.ABSTRACT)
            .addParameter("typeId", String::class)
            .returns(String::class.asTypeName().copy(nullable = true))
            .build()
        )
        .addFunction(FunSpec
            .builder("resolveTypeIdByKey")
            .addModifiers(KModifier.ABSTRACT)
            .addParameter("typeKey", String::class)
            .returns(String::class.asTypeName().copy(nullable = true))
            .build()
        )
        .build()

fun typedProductApiModule(config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder(ClassName(config.packageName, "TypedProductApiModule"))
        .addAnnotation(generated)
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
                TypedProductDeserializer(config).className
            )
            add(
                "setMixInAnnotation(%1T::class.java, %2L::class.java)\n",
                Product::class,
                "ProductMixIn"
            )
            add(
                "setMixInAnnotation(%1T::class.java, %2L::class.java)\n",
                ProductImpl::class,
                "FallbackProductMixIn"
            )
            add(
                "setDeserializerModifier(%1T())\n",
                TypedProductBeanDeserializerModifier(config).className,
            )
        })
        .build()

fun typedProductProjectionApiModule(config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder(ClassName(config.packageName, "TypedProductProjectionApiModule"))
        .addAnnotation(generated)
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
                ProductProjection::class,
                TypedProductProjectionDeserializer(config).className
            )
            add(
                "setMixInAnnotation(%1T::class.java, %2L::class.java)\n",
                ProductProjection::class,
                "ProductProjectionMixIn"
            )
            add(
                "setMixInAnnotation(%1T::class.java, %2L::class.java)\n",
                ProductProjectionImpl::class,
                "FallbackProductProjectionMixIn"
            )
            add(
                "setDeserializerModifier(%1T())\n",
                TypedProductProjectionBeanDeserializerModifier(config).className,
            )
        })
        .build()

fun typedResourcesApiModule(typedResourceFiles: List<TypedResources>, config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder(ClassName(config.packageName, "TypedResourcesApiModule"))
        .addAnnotation(generated)
        .superclass(SimpleModule::class)
        .primaryConstructor(FunSpec
            .constructorBuilder()
            .addParameter(ParameterSpec
                .builder("typeResolver", TypeResolver(config).className.parameterizedBy(Type::class.asTypeName()))
                .defaultValue("%T()", CustomFieldsTypeResolver(config).className)
                .build()
            )
            .build()
        )
        .addInitializerBlock(buildCodeBlock {
            if (hasReturnItemResources(typedResourceFiles)) {
                add(
                    "setMixInAnnotation(%1T::class.java, %2L::class.java)\n",
                    ReturnItem::class,
                    "ReturnItemMixIn"
                )
                add(
                    "setMixInAnnotation(%1T::class.java, %2L::class.java)\n",
                    ReturnItemImpl::class,
                    "FallbackReturnItemMixIn"
                )
                add(
                    "addDeserializer(%1T::class.java, %2T())\n",
                    ReturnItem::class,
                    ReturnItemDeserializer(config).className
                )
            }
            typedResourceFiles.forEach {
                add(
                    "addDeserializer(%1T::class.java, %2T(typeResolver))\n",
                    it.resourceInterface,
                    TypedResourceDeserializer(it).className
                )
                add(
                    "setMixInAnnotation(%1T::class.java, %2L::class.java)\n",
                    it.resourceInterface,
                    "${it.resourceInterface.simpleName}MixIn"
                )
                add(
                    "setMixInAnnotation(%1T::class.java, %2L::class.java)\n",
                    it.resourceDefaultImplementation,
                    "Fallback${it.resourceInterface.simpleName}MixIn"
                )
            }
            add(
                "setDeserializerModifier(%1T())\n",
                TypedCustomFieldsBeanDeserializerModifier(config).className,
            )
        })
        .build()

fun typedCustomObjectsApiModule(config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder(ClassName(config.packageName, "TypedCustomObjectsApiModule"))
        .addAnnotation(generated)
        .superclass(SimpleModule::class)
        .addInitializerBlock(buildCodeBlock {
            add(
                "addDeserializer(%1T::class.java, %2T())\n",
                CustomObject::class,
                TypedCustomObjectsDeserializer(config).className
            )
            add(
                "setMixInAnnotation(%1T::class.java, %2L::class.java)\n",
                CustomObject::class,
                "CustomObjectMixIn"
            )
            add(
                "setMixInAnnotation(%1T::class.java, %2L::class.java)\n",
                CustomObjectImpl::class,
                "FallbackCustomObjectMixIn"
            )
            add(
                "setDeserializerModifier(%1T())\n",
                TypedCustomObjectsBeanDeserializerModifier(config).className,
            )
        })
        .build()

fun productMixInInterface(config: Configuration): TypeSpec =
    TypeSpec
        .interfaceBuilder(ClassName(config.packageName, "ProductMixIn"))
        .addAnnotation(generated)
        .addAnnotation(deserializeAs(Product::class.asClassName()))
        .build()

fun fallbackProductInterface(config: Configuration) =
    TypeSpec
        .interfaceBuilder(ClassName(config.packageName, "FallbackProductMixIn"))
        .addAnnotation(generated)
        .addAnnotation(deserializeAs(ProductImpl::class.asClassName()))
        .build()

fun productProjectionMixInInterface(config: Configuration): TypeSpec =
    TypeSpec
        .interfaceBuilder(ClassName(config.packageName, "ProductProjectionMixIn"))
        .addAnnotation(generated)
        .addAnnotation(deserializeAs(ProductProjection::class.asClassName()))
        .build()

fun fallbackProjectionProductInterface(config: Configuration) =
    TypeSpec
        .interfaceBuilder(ClassName(config.packageName, "FallbackProductProjectionMixIn"))
        .addAnnotation(generated)
        .addAnnotation(deserializeAs(ProductProjectionImpl::class.asClassName()))
        .build()

fun returnItemMixInInterface(config: Configuration) =
    TypeSpec
        .interfaceBuilder(ClassName("${config.packageName}.return_item", "ReturnItemMixIn"))
        .addAnnotation(generated)
        .addAnnotation(AnnotationSpec.builder(JsonSubTypes::class).build())
        .addAnnotation(AnnotationSpec.builder(JsonTypeInfo::class).addMember("use = %T.NONE", Id::class).build())
        .addAnnotation(deserializeAs(ReturnItem::class.asClassName()))
        .build()

fun fallbackReturnItemInterface(config: Configuration) =
    TypeSpec
        .interfaceBuilder(ClassName("${config.packageName}.return_item", "FallbackReturnItemMixIn"))
        .addAnnotation(generated)
        .addAnnotation(deserializeAs(ReturnItemImpl::class.asClassName()))
        .build()

fun resourceMixInInterface(typedResourceFile: TypedResources, config: Configuration) =
    TypeSpec
        .interfaceBuilder(ClassName(config.packageName, "${typedResourceFile.resourceInterface.simpleName}MixIn"))
        .addAnnotation(generated)
        .addAnnotation(deserializeAs(typedResourceFile.resourceInterface.asClassName()))
        .build()

fun fallbackResourceInterface(typedResourceFile: TypedResources, config: Configuration) =
    TypeSpec
        .interfaceBuilder(ClassName(config.packageName, "Fallback${typedResourceFile.resourceInterface.simpleName}MixIn"))
        .addAnnotation(generated)
        .addAnnotation(deserializeAs(typedResourceFile.resourceDefaultImplementation.asClassName()))
        .build()

fun customObjectMixInInterface(config: Configuration): TypeSpec =
    TypeSpec
        .interfaceBuilder(ClassName(config.packageName, "CustomObjectMixIn"))
        .addAnnotation(generated)
        .addAnnotation(deserializeAs(CustomObject::class.asClassName()))
        .build()

fun fallbackCustomObjectInterface(config: Configuration) =
    TypeSpec
        .interfaceBuilder(ClassName(config.packageName, "FallbackCustomObjectMixIn"))
        .addAnnotation(generated)
        .addAnnotation(deserializeAs(CustomObjectImpl::class.asClassName()))
        .build()