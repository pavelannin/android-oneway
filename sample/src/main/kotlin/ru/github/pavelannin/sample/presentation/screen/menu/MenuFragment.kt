package ru.github.pavelannin.sample.presentation.screen.menu

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.jakewharton.rxbinding3.view.clicks
import kotlinx.android.synthetic.main.fragment_menu.*
import ru.github.pavelannin.sample.R

class MenuFragment : Fragment(R.layout.fragment_menu) {

    private val viewModel: MenuViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.navigationState.observe(owner = this) { action -> findNavController().navigate(action.consume()) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentMenuCounterButton.clicks()
            .map { MenuViewModel.Action.CountPressed }
            .subscribe(viewModel.actionSubject)

        fragmentMenuPaginationButton.clicks()
            .map { MenuViewModel.Action.PaginationPressed }
            .subscribe(viewModel.actionSubject)

        fragmentMenuDynamicAnimationButton.clicks()
            .map { MenuViewModel.Action.DynamicAnimationPressed }
            .subscribe(viewModel.actionSubject)
    }
}
