import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec

// foo-bar -> FooBar
// concat . map char1up . split '-'

val helloWorld = TypeSpec
    .classBuilder("Foo")
    .addModifiers(KModifier.DATA)
    .addProperty("id", String::class)
    .build()

print(helloWorld)


/*
*
* object UserListSerializer : JsonTransformingSerializer<List<User>>(ListSerializer(User.serializer())) {
    // If response is not an array, then it is a single object that should be wrapped into the array
    override fun transformDeserialize(element: JsonElement): JsonElement =
        if (element !is JsonArray) JsonArray(listOf(element)) else element
}
* */