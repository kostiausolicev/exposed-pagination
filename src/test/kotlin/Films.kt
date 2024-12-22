import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

object Films : IdTable<Int>("films") {
    override val id: Column<EntityID<Int>> = integer("id").autoIncrement().entityId()
    val name = varchar("film_name", 255)
    val director = varchar("director", 255)

    override val primaryKey = PrimaryKey(id)
}

object Actors : IdTable<Int>("actors") {
    override val id: Column<EntityID<Int>> = integer("id").autoIncrement().entityId()
    val name = varchar("actor_name", 255)

    override val primaryKey = PrimaryKey(id)
}

object FilmActors : IdTable<Int>("film_actors") {
    override val id: Column<EntityID<Int>> = integer("id").autoIncrement().entityId()
    val filmId = reference("film_id", Films)
    val actorId = reference("actor_id", Actors)


}