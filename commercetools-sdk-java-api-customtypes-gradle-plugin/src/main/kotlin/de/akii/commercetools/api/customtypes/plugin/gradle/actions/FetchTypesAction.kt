package de.akii.commercetools.api.customtypes.plugin.gradle.actions

import com.commercetools.api.client.QueryUtils
import com.commercetools.api.defaultconfig.ApiRootBuilder
import com.commercetools.api.defaultconfig.ServiceRegion
import com.commercetools.api.models.type.Type
import de.akii.commercetools.api.customtypes.plugin.gradle.parameters.FetchTypesParameters
import io.vrap.rmf.base.client.oauth2.ClientCredentials
import io.vrap.rmf.base.client.utils.json.JsonUtils
import org.gradle.workers.WorkAction

abstract class FetchTypesAction : WorkAction<FetchTypesParameters> {

    override fun execute() {
        val ctApi = ApiRootBuilder.of()
            .defaultClient(
                ClientCredentials.of()
                    .withClientId(parameters.clientId.get())
                    .withClientSecret(parameters.clientSecret.get())
                    .build(),
                ServiceRegion.valueOf(parameters.serviceRegion.get())
            )
            .build(parameters.projectKey.get())

        val types: List<Type> = QueryUtils
            .queryAll(
                ctApi.types().get(),
                java.util.function.Function.identity()
            )
            .toCompletableFuture()
            .get()
            .flatten()

        JsonUtils
            .createObjectMapper()
            .writeValue(parameters.typesFile.get(), types)
    }

}