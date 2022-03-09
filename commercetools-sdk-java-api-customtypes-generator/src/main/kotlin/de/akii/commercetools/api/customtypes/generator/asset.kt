package de.akii.commercetools.api.customtypes.generator

import com.commercetools.api.models.common.AssetImpl
import com.commercetools.api.models.common.AssetSource
import com.commercetools.api.models.common.LocalizedString
import com.commercetools.api.models.type.ResourceTypeId
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import de.akii.commercetools.api.customtypes.generator.common.*
import io.vrap.rmf.base.client.utils.Generated

fun typedAssetFile(config: Configuration): FileSpec =
    FileSpec
        .builder("${config.packageName}.asset", "TypedAsset")
        .addType(
            TypeSpec
                .classBuilder(ClassName(
                    "${config.packageName}.asset",
                    "TypedAsset"
                ))
                .addAnnotation(Generated::class)
                .superclass(AssetImpl::class)
                .addCTConstructorArguments(
                    CTParameter("id", String::class),
                    CTParameter("sources", List::class, AssetSource::class),
                    CTParameter("name", LocalizedString::class),
                    CTParameter("description", LocalizedString::class, nullable = true),
                    CTParameter("tags", List::class, String::class, nullable = true),
                    CTProperty("custom", resourceTypeIdToClassName(ResourceTypeId.ASSET, config), nullable = true),
                    CTParameter("key", String::class, nullable = true)
                )
                .build()
        )
        .build()
