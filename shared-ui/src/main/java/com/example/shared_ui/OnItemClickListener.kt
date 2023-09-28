package com.example.shared_ui

/**
 * Interface for handling click events on items of type [T].
 *
 * This interface is designed to be a general-purpose click listener for use
 * with various adapter views like RecyclerView. By providing the clicked item
 * directly as a parameter, it abstracts the logic of retrieving the item
 * based on position, thus making the client code cleaner.
 *
 * Example usage:
 *
 * ```
 * class MyAdapter(private val listener: OnItemClickListener<MyItem>) : RecyclerView.Adapter<...>() {
 *     // ...
 *     init {
 *         itemView.setOnClickListener {
 *             val item = getItem(bindingAdapterPosition)
 *             listener.onItemClick(item)
 *         }
 *     }
 * }
 * ```
 *
 * @param T The type of the items that will be clicked.
 */
interface OnItemClickListener<T> {
    /**
     * Called when an item of type [T] has been clicked.
     *
     * Implementations of this method can perform actions such as
     * navigating to a detail view, making a network request, etc.,
     * based on the clicked item.
     *
     * @param item The item that was clicked.
     */
    fun onItemClick(item: T)
}
