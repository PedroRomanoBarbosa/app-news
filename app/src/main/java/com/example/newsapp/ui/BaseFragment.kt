package com.example.newsapp.ui

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.example.newsapp.viewmodels.ScreenNavigation

abstract class BaseFragment<T : ScreenNavigation, S>(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId) {
    protected abstract fun onScreenNavigation(screenNavigation: ScreenNavigation)

    protected abstract fun onStateChanged(state: S)
}