package de.akii.commercetools.api.customtypes.generator

import com.commercetools.api.models.category.CategoryImpl
import com.commercetools.api.models.category.CategoryReference
import com.commercetools.api.models.common.Asset
import com.commercetools.api.models.common.CreatedBy
import com.commercetools.api.models.common.LastModifiedBy
import com.commercetools.api.models.common.LocalizedString
import com.commercetools.api.models.type.ResourceTypeId
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import de.akii.commercetools.api.customtypes.generator.common.*
import io.vrap.rmf.base.client.utils.Generated
import java.time.ZonedDateTime

fun typedCategoryFile(config: Configuration): FileSpec =
    FileSpec
        .builder("${config.packageName}.category", "TypedCategory")
        .addType(
            TypeSpec
                .classBuilder(ClassName(
                    "${config.packageName}.category",
                    "TypedCategory"
                ))
                .addAnnotation(Generated::class)
                .superclass(CategoryImpl::class)
                .addCTConstructorArguments(
                    CTParameter("id", String::class),
                    CTParameter("version", Long::class),
                    CTParameter("createdAt", ZonedDateTime::class),
                    CTParameter("lastModifiedAt", ZonedDateTime::class),
                    CTParameter("lastModifiedBy", LastModifiedBy::class, nullable = true),
                    CTParameter("createdBy", CreatedBy::class, nullable = true),
                    CTParameter("name", LocalizedString::class),
                    CTParameter("slug", LocalizedString::class),
                    CTParameter("description", LocalizedString::class, nullable = true),
                    CTParameter("ancestors", MutableList::class, CategoryReference::class),
                    CTParameter("parent", CategoryReference::class, nullable = true),
                    CTParameter("orderHint", String::class),
                    CTParameter("externalId", String::class, nullable = true),
                    CTParameter("metaTitle", LocalizedString::class, nullable = true),
                    CTParameter("metaDescription", LocalizedString::class, nullable = true),
                    CTParameter("metaKeywords", LocalizedString::class, nullable = true),
                    CTProperty("custom", resourceTypeIdToClassName(ResourceTypeId.CATEGORY, config), nullable = true),
                    CTParameter("assets", MutableList::class, Asset::class),
                    CTParameter("key", String::class, nullable = true)
                )
                .build()
        )
        .build()
