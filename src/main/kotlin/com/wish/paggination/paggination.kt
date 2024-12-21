package com.wish.paggination

import com.wish.paggination.model.PaginationRequest
import com.wish.paggination.model.PaginationResponse
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import kotlin.math.ceil

fun <T : IdTable<*>> T.paginate(request: PaginationRequest): PaginationResponse<ResultRow> {
    val page = request.page
    val size = request.size
    val totalElements = this.select(this.id).also { it applyFilter request.filter }.count()
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

private infix fun Query.applySort(sort: Pair<Expression<*>, SortOrder>?): Query = if (sort != null) {
    orderBy(sort.first, sort.second)
} else this

private infix fun Query.applyFilter(filter: Op<Boolean>?): Query = if (filter != null) where(filter) else this
