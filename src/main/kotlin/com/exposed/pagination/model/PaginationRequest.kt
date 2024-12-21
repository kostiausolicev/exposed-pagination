package com.exposed.pagination.model

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder

class PaginationRequest(
    val columns: List<Column<*>>,
    val page: Long,
    val size: Int,
    val filter: Op<Boolean>? = null,
    val sort: Pair<Expression<*>, SortOrder>? = null,
)