package com.exposed.pagination.model

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder

/**
 * PaginationRequest
 * @param columns - columns for select
 * @param page - page for select. Start with 1
 * @param size - count rows in selection
 * @param filter - filtering data in query
 * @param sort - column and directions for sort
 */
class PaginationRequest(
    val columns: List<Column<*>>,
    val page: Long,
    val size: Int,
    val filter: Op<Boolean>? = null,
    val sort: Pair<Expression<*>, SortOrder>? = null,
)