#Implementation for pagination for Exposed ORM

### pom.xml
```xml
<dependency>
    <groupId>com.exposed</groupId>
    <artifactId>pagination</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Usage
Have a IdTable Object `Films`
```kotlin
object Films : IdTable<Int>("films") {
    override val id: Column<EntityID<Int>> = integer("id").autoIncrement().entityId()
    val name = varchar("film_name", 255)
    val director = varchar("director", 255)
}
```

For paginate use syntax:
```kotlin
val paginateRequest = PaginationRequest(Films.columns, page = 1, size = 10)
val paginationResponse1 = transaction(db) { Films.paginate(paginateRequest) }
```

Result:

```json
{
  "content": [
    {
      "id": 1,
      "film_name": "film_name_1",
      "director": "film_name_1"
    }
  ],
  "page": 1,
  "size": 10,
  "isLast": true
}
```

Use sort:
```kotlin
val sort = Films.id to SortOrder.DESC
val paginateRequest = PaginationRequest(Films.columns, page = 1, size = 10, sort = sort)
val paginationResponse2 = transaction(db) { Films.paginate(paginateRequest) }
```

Use filter:
```kotlin
val filter = Op.build {  }
val paginateRequest = PaginationRequest(Films.columns, page = 1, size = 10, filter = filter)
val paginationResponse2 = transaction(db) { Films.paginate(paginateRequest) }
```

All paginate request should be called in transactional context
