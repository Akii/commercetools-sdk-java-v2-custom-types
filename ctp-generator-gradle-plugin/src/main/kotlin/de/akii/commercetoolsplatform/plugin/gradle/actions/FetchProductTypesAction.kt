package de.akii.commercetoolsplatform.plugin.gradle.actions

import com.commercetools.api.client.QueryUtils
import com.commercetools.api.defaultconfig.ApiRootBuilder
import com.commercetools.api.models.product_type.ProductType
import com.fasterxml.jackson.databind.ObjectMapper
import de.akii.commercetoolsplatform.plugin.gradle.parameters.RetrieveProductTypesParameters
import io.vrap.rmf.base.client.oauth2.ClientCredentials
import org.gradle.internal.impldep.com.google.common.base.Functions
import org.gradle.workers.WorkAction

abstract class FetchProductTypesAction : WorkAction<RetrieveProductTypesParameters> {

    override fun execute() {
        val ctpApi = ApiRootBuilder.of()
            .defaultClient(
                ClientCredentials.of()
                    .withClientId(parameters.clientId.get())
                    .withClientSecret(parameters.clientSecret.get())
                    .build(),
                parameters.serviceRegion.get()
            )
            .build(parameters.projectName.get())

        val productTypes: List<ProductType> = QueryUtils
            .queryAll(
                ctpApi.productTypes().get(),
                Functions.identity(),
                500
            )
            .toCompletableFuture()
            .get()
            .flatten()

        ObjectMapper().writeValue(parameters.productTypesFile.get(), productTypes)
    }

}
