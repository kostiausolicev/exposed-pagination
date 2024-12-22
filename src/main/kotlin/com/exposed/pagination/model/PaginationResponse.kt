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
) : Iterable<T> {

    override fun iterator(): Iterator<T> = PaginationIterator(this)

    fun <T, R> mapContent(transform: (T) -> R): PaginationResponse<R> {
        return PaginationResponse(content.map { transform(it as T) }, page, totalPages, page == totalPages)
    }

    /**
     * Pagination iterator. Make content parameter iterable
     */
    private class PaginationIterator<T>(
        private val response: PaginationResponse<T>
    ) : Iterator<T> {
        private var index = 0

        override fun hasNext(): Boolean = index < response.content.size

        override fun next(): T {
            if (!hasNext()) throw NoSuchElementException()
            return response.content[index++]
        }
    }
}