package com.github.vipulasri.timelineview.sample

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.github.vipulasri.timelineview.TimelineView
import com.github.vipulasri.timelineview.sample.model.Orientation
import com.github.vipulasri.timelineview.sample.model.TimelineAttributes
import com.github.vipulasri.timelineview.sample.widgets.BorderedCircle
import com.github.vipulasri.timelineview.sample.widgets.RoundedCornerBottomSheet
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.thebluealliance.spectrum.SpectrumDialog
import kotlinx.android.synthetic.main.bottom_sheet_options.*
import kotlinx.android.synthetic.main.item_bottom_sheet_line.*
import kotlinx.android.synthetic.main.item_bottom_sheet_marker.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class TimelineAttributesBottomSheet: RoundedCornerBottomSheet() {

    interface Callbacks {
        fun onAttributesChanged(attributes: TimelineAttributes)
    }

    companion object {

        private const val EXTRA_ATTRIBUTES = "EXTRA_ATTRIBUTES"

        fun showDialog(fragmentManager: FragmentManager, attributes: TimelineAttributes, callbacks: Callbacks) {
            val dialog = TimelineAttributesBottomSheet()
            dialog.arguments = bundleOf(
                    EXTRA_ATTRIBUTES to attributes
            )
            dialog.setCallback(callbacks)
            dialog.show(fragmentManager, "[TIMELINE_ATTRIBUTES_BOTTOM_SHEET]")
        }
    }

    private var mCallbacks: Callbacks? = null
    private lateinit var mAttributes: TimelineAttributes
    private var mBottomSheetBehavior: BottomSheetBehavior<*>? = null

    override fun onStart() {
        super.onStart()

        if (dialog != null) {
            val bottomSheet = dialog!!.findViewById<View>(R.id.design_bottom_sheet)
            bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }

        view?.post {
            val parent = view?.parent as View
            val params = parent.layoutParams as CoordinatorLayout.LayoutParams
            val behavior = params.behavior
            mBottomSheetBehavior = behavior as BottomSheetBehavior<*>?
            mBottomSheetBehavior?.peekHeight = view?.measuredHeight!!
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contextThemeWrapper = ContextThemeWrapper(activity, R.style.AppTheme)
        return inflater.cloneInContext(contextThemeWrapper).inflate(R.layout.bottom_sheet_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val attributes = (arguments!!.getParcelable(EXTRA_ATTRIBUTES) as TimelineAttributes)
        mAttributes = attributes.copy()

        text_attributes_heading.setOnClickListener { dismiss() }

        //orientation
        rg_orientation.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId) {
                R.id.rb_horizontal -> {
                    mAttributes.orientation = Orientation.HORIZONTAL
                }
                R.id.rb_vertical -> {
                    mAttributes.orientation = Orientation.VERTICAL
                }
            }
        }
        rg_orientation.check(if(mAttributes.orientation == Orientation.VERTICAL) R.id.rb_vertical else R.id.rb_horizontal)

        //marker

        image_marker_color.mFillColor = mAttributes.markerColor
        checkbox_marker_in_center.isChecked = mAttributes.markerInCenter


        checkbox_marker_in_center.setOnCheckedChangeListener { buttonView, isChecked ->
            mAttributes.markerInCenter = isChecked
        }

        image_marker_color.setOnClickListener { showColorPicker(mAttributes.markerColor, image_marker_color) }



        //line
        Log.e(" mAttributes.lineWidth", "${ mAttributes.lineWidth}")

        image_start_line_color.mFillColor = mAttributes.startLineColor
        image_end_line_color.mFillColor = mAttributes.endLineColor

        image_start_line_color.setOnClickListener { showColorPicker(mAttributes.startLineColor, image_start_line_color) }
        image_end_line_color.setOnClickListener { showColorPicker(mAttributes.endLineColor, image_end_line_color) }

        when(mAttributes.lineStyle) {
            TimelineView.LineStyle.NORMAL -> spinner_line_type.setSelection(0)
            TimelineView.LineStyle.DASHED -> spinner_line_type.setSelection(1)
        }

        spinner_line_type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                when (selectedItem) {
                    "Normal" -> mAttributes.lineStyle = TimelineView.LineStyle.NORMAL
                    "Dashed" -> mAttributes.lineStyle = TimelineView.LineStyle.DASHED
                    else -> {
                        mAttributes.lineStyle = TimelineView.LineStyle.NORMAL
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }



        button_apply.setOnClickListener {
            mCallbacks?.onAttributesChanged(mAttributes)
            dismiss()
        }
    }

    private fun showColorPicker(selectedColor : Int, colorView: BorderedCircle) {
        SpectrumDialog.Builder(requireContext())

                .setSelectedColor(selectedColor)
                .setDismissOnColorSelected(true)
                .setOutlineWidth(1)
                .setOnColorSelectedListener { positiveResult, color ->
                    if (positiveResult) {
                        colorView.mFillColor = color

                        when(colorView.id) {
                            R.id.image_marker_color ->  { mAttributes.markerColor = color }
                            R.id.image_start_line_color -> { mAttributes.startLineColor = color }
                            R.id.image_end_line_color -> { mAttributes.endLineColor = color }
                            else -> {
                                //do nothing
                            }
                        }

                    }
                }.build().show(childFragmentManager, "ColorPicker")
    }



    private fun setCallback(callbacks: Callbacks) {
        mCallbacks = callbacks
    }

}