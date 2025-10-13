package com.example.butterflydetector.ui.base

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.butterflydetector.utils.ColorModeManager

open class BaseFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyColorMode(view)
    }

    override fun onResume() {
        super.onResume()
        view?.let { applyColorMode(it) }
    }

    protected open fun applyColorMode(view: View) {
        // should be overridden in child fragments
        // to apply specific colors
    }

    protected fun getLogoGreen(): Int {
        return ColorModeManager.getLogoGreen(requireContext())
    }

    protected fun getLogoDarkGreen(): Int {
        return ColorModeManager.getLogoDarkGreen(requireContext())
    }

    protected fun getBookPages(): Int {
        return ColorModeManager.getBookPages(requireContext())
    }

    protected fun getPurple700(): Int {
        return ColorModeManager.getPurple700(requireContext())
    }
}