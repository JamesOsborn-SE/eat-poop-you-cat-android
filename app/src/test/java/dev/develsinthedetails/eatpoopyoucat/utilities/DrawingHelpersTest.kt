package dev.develsinthedetails.eatpoopyoucat.utilities

import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.DefaultAsserter.assertEquals
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class DrawingHelpersTest {

    @Test
    fun `generator creates ProfileGenerator from UUID is stable`() {
        val uuid = Uuid.parse("550e840a-0102-0304-0506-0708090a0b0c")
//        val uuid = Uuid.parse("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF")
        val expectedResult= Json.decodeFromString<Profile>("""{"coordinates":[{"x":0.99969643,"y":0.024637451},{"x":0.998786,"y":0.049259946},{"x":0.99726915,"y":0.07385253},{"x":0.99514693,"y":0.09840029},{"x":0.9924205,"y":0.1228883},{"x":0.98909163,"y":0.1473017},{"x":0.98516226,"y":0.17162567},{"x":0.98063475,"y":0.19584548},{"x":0.97551197,"y":0.21994637},{"x":0.96979696,"y":0.24391373},{"x":0.96349317,"y":0.267733},{"x":0.9566044,"y":0.29138976}],"cornerRadius":10.0,"backgroundColor":{"value":18432332551465992192},"color":{"value":18398627812790501376}}""")
        val result = generateProfile(uuid)
        val oof = Json.encodeToString(result)
        print(oof)
        assertEquals("Colors match", expectedResult.color,result.color)
        assertEquals("Offsets match", expectedResult.coordinates, result.coordinates)
    }
}
