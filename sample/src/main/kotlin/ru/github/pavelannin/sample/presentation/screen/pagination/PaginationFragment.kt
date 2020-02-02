package ru.github.pavelannin.sample.presentation.screen.pagination

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_pagination.*
import ru.github.pavelannin.oneway.lifecycle.map
import ru.github.pavelannin.sample.R
import java.util.concurrent.TimeUnit

class PaginationFragment : Fragment(R.layout.fragment_pagination) {

    private val viewModel: PaginationViewModel by viewModels()
    private val paginationCountSubject: PublishSubject<Int> = PublishSubject.create()
    private val paginationAdapter by lazy(mode = LazyThreadSafetyMode.NONE) { PaginationAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentPaginationRecyclerView.adapter = paginationAdapter
        viewModel.state.map(transform = ::mapToViewState).observe(viewLifecycleOwner, ::render)

        Observable.combineLatest(
            paginationCountSubject.distinctUntilChanged(),
            paginationAdapter.lastDisplayedIndexSubject
                .distinctUntilChanged()
                .scan(Pair(0, 0)) { accumulator, index -> accumulator.second to index }
                .filter { (previousIndex, currentIndex) -> currentIndex > previousIndex }
                .map { (_, currentIndex) -> currentIndex },
            BiFunction<Int, Int, Pair<Int, Int>> { count, index -> count to index }
        )
            .filter { (count, index) -> ((count - 1) - index) <= 10 }
            .map { PaginationViewModel.ExternalAction.LoadMore }
            .throttleFirst(500, TimeUnit.MILLISECONDS)
            .subscribe(viewModel.actionSubject)
    }

    private fun render(state: ViewState) {
        paginationCountSubject.onNext(state.content.count())
        paginationAdapter.submitList(state.content)
        fragmentPaginationProgressIndicator.isVisible = state.isLoading
    }

    private data class ViewState(
        val isLoading: Boolean,
        val content: List<PaginationAdapter.ViewState>
    )

    private fun mapToViewState(state: PaginationViewModel.State): ViewState {
        return ViewState(
            isLoading = state.loadingStatus is PaginationViewModel.State.Response.Loading && state.data.isEmpty(),
            content = state.data.map { PaginationAdapter.ViewState.Loaded(count = it.toString()) }
                .toList()
                .plus(
                    { listOf(PaginationAdapter.ViewState.Loading) }
                        .takeIf { state.loadingStatus is PaginationViewModel.State.Response.Loading && state.data.isNotEmpty() }
                        ?.invoke()
                        ?: emptyList()
                )
        )
    }
}
