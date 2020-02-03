package ru.github.pavelannin.sample.presentation.screen.dynamic_animation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.map
import androidx.lifecycle.observe
import com.github.lcdsmao.springx.ViewPropertySpringAnimator
import com.github.lcdsmao.springx.spring
import com.jakewharton.rxbinding3.view.clicks
import kotlinx.android.synthetic.main.fragment_dynamic_animation.*
import ru.github.pavelannin.sample.R

class DynamicAnimationFragment : Fragment(R.layout.fragment_dynamic_animation) {

    private val viewModel: DynamicAnimationViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.state.map(::mapToViewState).observe(viewLifecycleOwner, ::render)

        fragmentDynamicAnimationCleanButton.clicks()
            .map { DynamicAnimationViewModel.Action.Clean }
            .subscribe(viewModel.actionSubject)

        fragmentDynamicAnimationToTopLeftButton.clicks()
            .map { DynamicAnimationViewModel.Action.ToTopLeft }
            .subscribe(viewModel.actionSubject)

        fragmentDynamicAnimationToTopRightButton.clicks()
            .map { DynamicAnimationViewModel.Action.ToTopRight }
            .subscribe(viewModel.actionSubject)
    }

    private fun render(state: ViewState) {
        fragmentDynamicAnimationCleanButton.isEnabled = state.isCleanEnabled
        fragmentDynamicAnimationToTopLeftButton.isEnabled = state.isTopLeftEnabled
        fragmentDynamicAnimationToTopRightButton.isEnabled = state.isTopRightEnabled
        state.animationApplier(fragmentDynamicAnimationView).start()
    }

    private data class ViewState(
        val isCleanEnabled: Boolean,
        val isTopLeftEnabled: Boolean,
        val isTopRightEnabled: Boolean,
        val animationApplier: (View) -> ViewPropertySpringAnimator<View>
    )

    private fun mapToViewState(state: DynamicAnimationViewModel.State): ViewState {
        return ViewState(
            isCleanEnabled = state.animationEndPoint != DynamicAnimationViewModel.State.AnimationEndPoint.Start,
            isTopLeftEnabled = state.animationEndPoint != DynamicAnimationViewModel.State.AnimationEndPoint.TopLeft,
            isTopRightEnabled = state.animationEndPoint != DynamicAnimationViewModel.State.AnimationEndPoint.TopRight,
            animationApplier = when (state.animationEndPoint) {
                DynamicAnimationViewModel.State.AnimationEndPoint.Start -> { view: View ->
                    view.spring()
                        .defaultDampingRatio(1f)
                        .defaultStiffness(8f)
                        .translationX(0f)
                        .translationY(0f)
                }
                DynamicAnimationViewModel.State.AnimationEndPoint.TopLeft -> { view: View ->
                    view.spring()
                        .defaultDampingRatio(1f)
                        .defaultStiffness(8f)
                        .translationX(-300f)
                        .translationY(-1500f)
                }
                DynamicAnimationViewModel.State.AnimationEndPoint.TopRight -> { view: View ->
                    view.spring()
                        .defaultDampingRatio(1f)
                        .defaultStiffness(8f)
                        .translationX(300f)
                        .translationY(-1500f)
                }
            }
        )
    }
}
