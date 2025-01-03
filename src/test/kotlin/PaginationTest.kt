import com.exposed.pagination.model.PaginationRequest
import com.exposed.pagination.paginate
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.JoinType
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
    fun `join filter test`() {
        val pageSize = 50
        val paginateRequest = PaginationRequest(listOf(
            Films.id,
            Actors.id
        ), page = 1, size = pageSize, filter = Op.build { Actors.id lessEq 10 })
        val actors = transaction(db) {
            Films.join(FilmActors, JoinType.INNER, FilmActors.filmId, Films.id)
                .join(Actors, JoinType.FULL, FilmActors.actorId, Actors.id)
                .paginate(paginateRequest)
        }
        assert(actors.isLast && actors.content.size == 10 && actors.content.all { row -> row[Actors.id].value <= 10 })
    }

    @Test
    fun `join sort test`() {
        var actorId = TOTAL_ACTORS
        val pageSize = 100
        val paginateRequest = PaginationRequest(listOf(
            Films.id,
            Actors.id
        ), page = 1, size = pageSize, sort = Actors.id to SortOrder.DESC)
        val actors = transaction(db) {
            Films.join(FilmActors, JoinType.RIGHT, FilmActors.filmId, Films.id)
                .join(Actors, JoinType.RIGHT, FilmActors.actorId, Actors.id)
                .paginate(paginateRequest)
        }
        assert(actors.isLast && actors.content.size == pageSize)
        for (f in actors.content) {
            assert(f[Actors.id].value == actorId--)
        }
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
        assert(paginationResponse3.isLast && paginationResponse3.content.size == (TOTAL_FILMS - 2 * pageSize))
        for (f in paginationResponse3.content) {
            assert(f[Films.id].value == id++)
        }
    }

    @Test
    fun `filter paginate`() {
        val pageSize = 100
        val filter = Op.build { (Films.id lessEq TOTAL_FILMS) and (Films.id greater TOTAL_FILMS - pageSize * 2) }
        val paginateRequest1 = PaginationRequest(Films.columns, page = 1, size = pageSize, filter = filter)
        val paginationResponse1 = transaction(db) { Films.paginate(paginateRequest1) }
        assert(!paginationResponse1.isLast && paginationResponse1.content.size == pageSize)
        assert(paginationResponse1.content.all { row -> row[Films.id].value in (TOTAL_FILMS - pageSize * 2)..TOTAL_FILMS }
                && paginationResponse1.content.last()[Films.id].value == (TOTAL_FILMS - pageSize)
        )

        val paginateRequest2 = PaginationRequest(Films.columns, page = 2, size = pageSize, filter = filter)
        val paginationResponse2 = transaction(db) { Films.paginate(paginateRequest2) }
        assert(paginationResponse2.isLast && paginationResponse2.content.size == pageSize)
        assert(paginationResponse2.content.all { row -> row[Films.id].value in (TOTAL_FILMS - pageSize * 2)..TOTAL_FILMS }
                && paginationResponse2.content.last()[Films.id].value == (TOTAL_FILMS)
        )
    }

    @Test
    fun `sort paginate`() {
        var id = TOTAL_FILMS
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
        assert(paginationResponse3.isLast && paginationResponse3.content.size == (TOTAL_FILMS - 2 * pageSize))
        for (f in paginationResponse3.content) {
            assert(f[Films.id].value == id--)
        }
    }

    @Test
    fun `sort and filter paginate`() {
        val sort = Films.id to SortOrder.DESC
        val pageSize = 100
        val filter = Op.build { (Films.id lessEq TOTAL_FILMS) and (Films.id greater TOTAL_FILMS - pageSize * 2) }
        val paginateRequest1 = PaginationRequest(Films.columns, page = 1, size = pageSize, filter = filter, sort = sort)
        val paginationResponse1 = transaction(db) { Films.paginate(paginateRequest1) }
        assert(!paginationResponse1.isLast && paginationResponse1.content.size == pageSize)
        assert(paginationResponse1.content.all { row -> row[Films.id].value in (TOTAL_FILMS - pageSize * 2)..TOTAL_FILMS }
                && paginationResponse1.content.last()[Films.id].value == (TOTAL_FILMS - pageSize + 1)
        )

        val paginateRequest2 = PaginationRequest(Films.columns, page = 2, size = pageSize, filter = filter, sort = sort)
        val paginationResponse2 = transaction(db) { Films.paginate(paginateRequest2) }
        assert(paginationResponse2.isLast && paginationResponse2.content.size == pageSize)
        assert(paginationResponse2.content.all { row -> row[Films.id].value in (TOTAL_FILMS - pageSize * 2)..TOTAL_FILMS }
                && paginationResponse2.content.last()[Films.id].value == (TOTAL_FILMS - 2 * pageSize + 1)
        )
    }

    companion object {
        const val FILM_NAME_PREFIX = "FILM_"
        const val DIRECTOR_PREFIX = "DIRECTOR_"
        const val TOTAL_FILMS = 1000
        const val TOTAL_ACTORS = 100
        var db: Database? = null

        @BeforeAll
        @JvmStatic
        fun setUpConnection() {
            var actorId = 1
            var postgres = PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
            postgres.start()
            db = Database.connect(
                postgres.jdbcUrl,
                driver = "org.postgresql.Driver",
                user = postgres.username,
                password = postgres.password
            )

            transaction(db) {
                SchemaUtils.create(Films, Actors, FilmActors)
            }

            transaction(db) {
                for (i in 0..(TOTAL_FILMS - 1)) {
                    Films.insert {
                        it[name] = "${FILM_NAME_PREFIX}$i"
                        it[director] = "${DIRECTOR_PREFIX}$i"
                    }
                }
            }

            transaction(db) {
                for (i in 1..TOTAL_ACTORS) {
                    Actors.insert {
                        it[name] = "ACTOR_$actorId"
                    }
                }
            }

            transaction(db) {
                for (i in 1..TOTAL_ACTORS) {
                    FilmActors.insert {
                        it[FilmActors.actorId] = i
                        it[filmId] = i + 2
                    }
                }
            }
        }
    }
}