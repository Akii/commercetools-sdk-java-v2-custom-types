package de.akii.commercetools.api.customtypes.generator

import com.commercetools.api.models.product.Product
import com.fasterxml.jackson.databind.module.SimpleModule

class CustomProductApiModule : SimpleModule() {

    init {
        // addDeserializer(Product::class.java, productDeserializer)
    }

}