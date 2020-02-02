package ru.github.pavelannin.sample.presentation.screen.counter

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.jakewharton.rxbinding3.view.clicks
import kotlinx.android.synthetic.main.fragment_counter.*
import ru.github.pavelannin.oneway.lifecycle.map
import ru.github.pavelannin.sample.R

class CounterFragment : Fragment(R.layout.fragment_counter) {

    private val viewModel: CounterViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.state.map(transform = ::mapToViewState).observe(viewLifecycleOwner, ::render)

        fragmentCounterDecrementButton.clicks()
            .map { CounterViewModel.Action.Decrement }
            .subscribe(viewModel.actionSubject)

        fragmentCounterIncrementButton.clicks()
            .map { CounterViewModel.Action.Increment }
            .subscribe(viewModel.actionSubject)
    }

    private fun render(state: ViewState) {
        fragmentCounterTextView.text = state.count
    }

    private data class ViewState(val count: String)

    private fun mapToViewState(state: CounterViewModel.State): ViewState {
        return ViewState(count = state.count.toString())
    }
}
