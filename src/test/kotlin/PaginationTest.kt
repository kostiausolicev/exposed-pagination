import com.wish.paggination.model.PaginationRequest
import com.wish.paggination.paginate
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import kotlin.test.Test

class PaginationTest {
    @Test
    fun `database connection test`() {
        assert(db != null)
    }

    @Test
    fun `simple paginate`() {
        var id = 1
        val pageSize = 499
        val paginateRequest1 = PaginationRequest(Films.columns, page = 1, size = pageSize)
        val paginationResponse1 = transaction(db) { Films.paginate(paginateRequest1) }
        assert(!paginationResponse1.isLast && paginationResponse1.content.size == pageSize)
        for (f in paginationResponse1.content) {
            assert(f[Films.id].value == id++)
        }
        val paginateRequest2 = PaginationRequest(Films.columns, page = 2, size = pageSize)
        val paginationResponse2 = transaction(db) { Films.paginate(paginateRequest2) }
        assert(!paginationResponse2.isLast && paginationResponse2.content.size == pageSize)
        for (f in paginationResponse2.content) {
            assert(f[Films.id].value == id++)
        }
        val paginateRequest3 = PaginationRequest(Films.columns, page = 3, size = pageSize)
        val paginationResponse3 = transaction(db) { Films.paginate(paginateRequest3) }
        assert(paginationResponse3.isLast && paginationResponse3.content.size == (TOTAL_ELEMENTS - 2 * pageSize))
        for (f in paginationResponse3.content) {
            assert(f[Films.id].value == id++)
        }
    }

    @Test
    fun `filter paginate`() {
        val pageSize = 100
        val filter = Op.build { (Films.id lessEq TOTAL_ELEMENTS) and (Films.id greater TOTAL_ELEMENTS - pageSize * 2) }
        val paginateRequest1 = PaginationRequest(Films.columns, page = 1, size = pageSize, filter = filter)
        val paginationResponse1 = transaction(db) { Films.paginate(paginateRequest1) }
        assert(!paginationResponse1.isLast && paginationResponse1.content.size == pageSize)
        assert(paginationResponse1.content.all { row -> row[Films.id].value in (TOTAL_ELEMENTS - pageSize * 2)..TOTAL_ELEMENTS }
                && paginationResponse1.content.last()[Films.id].value == (TOTAL_ELEMENTS - pageSize)
        )

        val paginateRequest2 = PaginationRequest(Films.columns, page = 2, size = pageSize, filter = filter)
        val paginationResponse2 = transaction(db) { Films.paginate(paginateRequest2) }
        assert(paginationResponse2.isLast && paginationResponse2.content.size == pageSize)
        assert(paginationResponse2.content.all { row -> row[Films.id].value in (TOTAL_ELEMENTS - pageSize * 2)..TOTAL_ELEMENTS }
                && paginationResponse2.content.last()[Films.id].value == (TOTAL_ELEMENTS)
        )
    }

    @Test
    fun `sort paginate`() {
        var id = TOTAL_ELEMENTS
        val sort = Films.id to SortOrder.DESC
        val pageSize = 499
        val paginateRequest1 = PaginationRequest(Films.columns, page = 1, size = pageSize, sort = sort)
        val paginationResponse1 = transaction(db) { Films.paginate(paginateRequest1) }
        assert(!paginationResponse1.isLast && paginationResponse1.content.size == pageSize)
        for (f in paginationResponse1.content) {
            assert(f[Films.id].value == id--)
        }
        val paginateRequest2 = PaginationRequest(Films.columns, page = 2, size = pageSize, sort = sort)
        val paginationResponse2 = transaction(db) { Films.paginate(paginateRequest2) }
        assert(!paginationResponse2.isLast && paginationResponse2.content.size == pageSize)
        for (f in paginationResponse2.content) {
            assert(f[Films.id].value == id--)
        }
        val paginateRequest3 = PaginationRequest(Films.columns, page = 3, size = pageSize, sort = sort)
        val paginationResponse3 = transaction(db) { Films.paginate(paginateRequest3) }
        assert(paginationResponse3.isLast && paginationResponse3.content.size == (TOTAL_ELEMENTS - 2 * pageSize))
        for (f in paginationResponse3.content) {
            assert(f[Films.id].value == id--)
        }
    }

    @Test
    fun `sort and filter paginate`() {
        val sort = Films.id to SortOrder.DESC
        val pageSize = 100
        val filter = Op.build { (Films.id lessEq TOTAL_ELEMENTS) and (Films.id greater TOTAL_ELEMENTS - pageSize * 2) }
        val paginateRequest1 = PaginationRequest(Films.columns, page = 1, size = pageSize, filter = filter, sort = sort)
        val paginationResponse1 = transaction(db) { Films.paginate(paginateRequest1) }
        assert(!paginationResponse1.isLast && paginationResponse1.content.size == pageSize)
        assert(paginationResponse1.content.all { row -> row[Films.id].value in (TOTAL_ELEMENTS - pageSize * 2)..TOTAL_ELEMENTS }
                && paginationResponse1.content.last()[Films.id].value == (TOTAL_ELEMENTS - pageSize + 1)
        )

        val paginateRequest2 = PaginationRequest(Films.columns, page = 2, size = pageSize, filter = filter, sort = sort)
        val paginationResponse2 = transaction(db) { Films.paginate(paginateRequest2) }
        assert(paginationResponse2.isLast && paginationResponse2.content.size == pageSize)
        assert(paginationResponse2.content.all { row -> row[Films.id].value in (TOTAL_ELEMENTS - pageSize * 2)..TOTAL_ELEMENTS }
                && paginationResponse2.content.last()[Films.id].value == (TOTAL_ELEMENTS - 2 * pageSize + 1)
        )
    }

    object Films : IdTable<Int>("films") {
        override val id: Column<EntityID<Int>> = integer("id").autoIncrement().entityId()
        val name = varchar("film_name", 255)
        val director = varchar("director", 255)
    }

    companion object {
        const val FILM_NAME_PREFIX = "FILM_"
        const val DIRECTOR_PREFIX = "DIRECTOR_"
        const val TOTAL_ELEMENTS = 1000
        var db: Database? = null

        @BeforeAll
        @JvmStatic
        fun setUpConnection() {
            var postgres = PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
            postgres.start()
            db = Database.connect(
                postgres.jdbcUrl,
                driver = "org.postgresql.Driver",
                user = postgres.username,
                password = postgres.password
            )

            transaction(db) {
                SchemaUtils.create(Films)
            }

            transaction(db) {
                for (i in 0..(TOTAL_ELEMENTS - 1)) {
                    Films.insert {
                        it[name] = "${FILM_NAME_PREFIX}$i"
                        it[director] = "${DIRECTOR_PREFIX}$i"
                    }
                }
            }
        }
    }
}