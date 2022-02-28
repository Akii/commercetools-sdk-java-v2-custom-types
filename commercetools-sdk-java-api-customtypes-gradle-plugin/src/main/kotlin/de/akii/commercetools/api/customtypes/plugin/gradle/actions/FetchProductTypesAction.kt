package de.akii.commercetools.api.customtypes.plugin.gradle.actions

import com.commercetools.api.client.QueryUtils
import com.commercetools.api.defaultconfig.ApiRootBuilder
import com.commercetools.api.defaultconfig.ServiceRegion
import com.commercetools.api.models.product_type.ProductType
import de.akii.commercetools.api.customtypes.plugin.gradle.parameters.FetchProductTypesParameters
import io.vrap.rmf.base.client.oauth2.ClientCredentials
import io.vrap.rmf.base.client.utils.json.JsonUtils
import org.gradle.workers.WorkAction

abstract class FetchProductTypesAction : WorkAction<FetchProductTypesParameters> {

    override fun execute() {
        val ctApi = ApiRootBuilder.of()
            .defaultClient(
                ClientCredentials.of()
                    .withClientId(parameters.clientId.get())
                    .withClientSecret(parameters.clientSecret.get())
                    .build(),
                ServiceRegion.valueOf(parameters.serviceRegion.get())
            )
            .build(parameters.projectName.get())

        val productTypes: List<ProductType> = QueryUtils
            .queryAll(
                ctApi.productTypes().get(),
                java.util.function.Function.identity()
            )
            .toCompletableFuture()
            .get()
            .flatten()

        JsonUtils
            .createObjectMapper()
            .writeValue(parameters.productTypesFile.get(), productTypes)
    }

}
