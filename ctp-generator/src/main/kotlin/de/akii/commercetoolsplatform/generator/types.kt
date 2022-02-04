package de.akii.commercetoolsplatform.generator

import de.akii.commercetoolsplatform.common.CreatedBy
import de.akii.commercetoolsplatform.common.LastModifiedBy
import de.akii.commercetoolsplatform.common.LocalizedString
import de.akii.commercetoolsplatform.common.Reference
import java.time.LocalDateTime

interface ProductAttributes

data class Product<T : ProductAttributes>(
    val id: String,
    val version: Int,
    val lastMessageSequenceNumber: Int,
    val createdAt: LocalDateTime,
    val createdBy: CreatedBy,
    val lastModifiedAt: LocalDateTime,
    val lastModifiedBy: LastModifiedBy,
    val productType: Reference,
    val lastVariantId: Int,
    val masterData: MasterData<T>
)

data class MasterData<T : ProductAttributes>(
    val current: ProductData<T>,
    val staged: ProductData<T>,
    val published: Boolean,
    val hasStagedChanges: Boolean
)

data class ProductData<T : ProductAttributes>(
    val name: LocalizedString,
    val categories: List<Reference>,
    val categoryOrderHints: Map<String, String> = emptyMap(),
    val description: LocalizedString? = null,
    val slug: LocalizedString,
    val metaTitle: LocalizedString? = null,
    val metaDescription: LocalizedString? = null,
    val metaKeywords: LocalizedString? = null,
    val masterVariant: ProductVariant<T>,
    val variants: List<ProductVariant<T>>,
    val searchKeywords: Map<String, Any> = emptyMap()
)

data class ProductVariant<T : ProductAttributes>(
    val attributes: T? = null
)