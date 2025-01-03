import com.exposed.pagination.model.PaginationResponse
import kotlin.test.Test

class UtilTest {
    @Test
    fun `map test`() {
        val paginationResponse = PaginationResponse(
            listOf(1, 2, 3, 4, 5),
            1,
            1,
            true
        )
        val mapped = paginationResponse.map { it * 2 }
        assert(mapped.content == listOf(2, 4, 6, 8, 10))
        assert(mapped.page == 1L)
        assert(mapped.totalPages == 1L)
        assert(mapped.isLast)
    }
}