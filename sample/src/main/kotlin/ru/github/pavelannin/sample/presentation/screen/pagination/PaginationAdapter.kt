package ru.github.pavelannin.sample.presentation.screen.pagination

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_pagination.view.*
import ru.github.pavelannin.sample.R

class PaginationAdapter : ListAdapter<PaginationAdapter.ViewState, PaginationAdapter.ViewHolder>(ViewState) {

    private val _lastDisplayedIndexSubject: PublishSubject<Int> = PublishSubject.create()

    val lastDisplayedIndexSubject: Observable<Int>
        get() = _lastDisplayedIndexSubject

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater by lazy(mode = LazyThreadSafetyMode.NONE) { LayoutInflater.from(parent.context) }
        val viewCreator = fun(): View { return inflater.inflate(viewType, parent, false) }
        return when (viewType) {
            R.layout.item_pagination -> ViewHolder.Data(viewCreator())
            R.layout.item_progress -> ViewHolder.ProgressIndicator(viewCreator())
            else -> error("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        _lastDisplayedIndexSubject.onNext(position)
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = when (holder) {
        is ViewHolder.Data -> holder.render(state = checkNotNull(getItem(position) as? ViewState.Loaded))
        is ViewHolder.ProgressIndicator -> {
            /* nothing */
        }
    }

    override fun getItemViewType(position: Int): Int {
        super.getItemViewType(position)
        return when (getItem(position)) {
            is ViewState.Loaded -> R.layout.item_pagination
            ViewState.Loading -> R.layout.item_progress
        }
    }

    sealed class ViewState {
        data class Loaded(val count: String) : ViewState()
        object Loading : ViewState()

        companion object : DiffUtil.ItemCallback<ViewState>() {

            override fun areItemsTheSame(oldItem: ViewState, newItem: ViewState): Boolean = when {
                oldItem is Loaded && newItem is Loaded -> oldItem == newItem
                oldItem is Loading && newItem is Loading -> oldItem == newItem
                else -> false
            }

            override fun areContentsTheSame(oldItem: ViewState, newItem: ViewState): Boolean {
                return areItemsTheSame(oldItem, newItem)
            }
        }
    }

    sealed class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), LayoutContainer {

        class Data(override val containerView: View) : ViewHolder(containerView) {

            fun render(state: ViewState.Loaded) {
                containerView.itemPaginationTextView.text = state.count
            }
        }

        class ProgressIndicator(override val containerView: View) : ViewHolder(containerView)
    }
}
