package com.exposed.pagination

import com.exposed.pagination.model.PaginationRequest
import com.exposed.pagination.model.PaginationResponse
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.ColumnSet
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import kotlin.math.ceil

/**
 * Paginate implementation.
 * @param request - pagination request
 *
 * @return PaginationResponse with type ResultRow
 *
 * @see PaginationRequest
 * @see PaginationResponse
 * @see ResultRow
 */
fun <T : ColumnSet> T.paginate(request: PaginationRequest): PaginationResponse<ResultRow> {
    val page = request.page
    val size = request.size
    val totalElements = this.select(request.columns.first()).also { it applyFilter request.filter }.count()
    val contentQuery = this.select(request.columns)
        .offset((page - 1) * size)
        .also {
            it applyFilter request.filter
            it applySort request.sort
        }
    val content = contentQuery.limit(size).toList()
    val totalPages = ceil(totalElements.toDouble() / size).toLong()
    return PaginationResponse(content, page, totalPages, page == totalPages)
}

/**
 * Function for applying sort
 * @param sort - pair with a sort field and sort direction
 *
 * @see SortOrder
 * @see Expression
 */
private infix fun Query.applySort(sort: Pair<Expression<*>, SortOrder>?): Query = if (sort != null) { orderBy(sort.first, sort.second) } else this

/**
 * Function for applying filter
 * @param filter - filter for database query with Op syntax
 * @see Op
 */
private infix fun Query.applyFilter(filter: Op<Boolean>?): Query = if (filter != null) where(filter) else this
