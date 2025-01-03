package com.exposed.pagination.model

/**
 * PaginationResponse
 * @param content - elements on page
 * @param page - current page
 * @param totalPages - total pages
 * @param isLast - is last
 */
@Suppress("UNCHECKED_CAST")
class PaginationResponse<T>(
    val content: List<T>,
    val page: Long,
    val totalPages: Long,
    val isLast: Boolean
) {
    fun <R> map(transform: (T) -> R): PaginationResponse<R> {
        return PaginationResponse(content.map(transform), page, totalPages, isLast)
    }
}