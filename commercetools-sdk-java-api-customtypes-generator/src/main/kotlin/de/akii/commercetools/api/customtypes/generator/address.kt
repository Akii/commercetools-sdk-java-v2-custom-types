package de.akii.commercetools.api.customtypes.generator

import com.commercetools.api.models.common.AddressImpl
import com.commercetools.api.models.type.ResourceTypeId
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import de.akii.commercetools.api.customtypes.generator.common.*
import io.vrap.rmf.base.client.utils.Generated

fun typedAddressFile(config: Configuration): FileSpec =
    FileSpec
        .builder("${config.packageName}.address", "TypedAddress")
        .addType(
            TypeSpec
                .classBuilder(ClassName(
                    "${config.packageName}.address",
                    "TypedAddress"
                ))
                .addAnnotation(Generated::class)
                .superclass(AddressImpl::class)
                .addCTConstructorArguments(
                    CTParameter("id", String::class, nullable = true),
                    CTParameter("key", String::class, nullable = true),
                    CTParameter("title", String::class, nullable = true),
                    CTParameter("salutation", String::class, nullable = true),
                    CTParameter("firstName", String::class, nullable = true),
                    CTParameter("lastName", String::class, nullable = true),
                    CTParameter("streetName", String::class, nullable = true),
                    CTParameter("streetNumber", String::class, nullable = true),
                    CTParameter("additionalStreetInfo", String::class, nullable = true),
                    CTParameter("postalCode", String::class, nullable = true),
                    CTParameter("city", String::class, nullable = true),
                    CTParameter("region", String::class, nullable = true),
                    CTParameter("state", String::class, nullable = true),
                    CTParameter("country", String::class),
                    CTParameter("company", String::class, nullable = true),
                    CTParameter("department", String::class, nullable = true),
                    CTParameter("building", String::class, nullable = true),
                    CTParameter("apartment", String::class, nullable = true),
                    CTParameter("pOBox", String::class, nullable = true),
                    CTParameter("phone", String::class, nullable = true),
                    CTParameter("mobile", String::class, nullable = true),
                    CTParameter("email", String::class, nullable = true),
                    CTParameter("fax", String::class, nullable = true),
                    CTParameter("additionalAddressInfo", String::class, nullable = true),
                    CTParameter("externalId", String::class, nullable = true),
                    CTProperty("custom", resourceTypeIdToClassName(ResourceTypeId.ADDRESS, config), nullable = true)
                )
                .build()
        )
        .build()
