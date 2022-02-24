package ru.rizz.slideshow.common

import android.view.*
import android.widget.*
import androidx.databinding.*

@BindingAdapter("android:visibility")
fun View.setVisibilityBinding(isVisible: Boolean) {
	visibility = if (isVisible) View.VISIBLE else View.GONE
}

@BindingAdapter("is24HourView")
fun TimePicker.setIs24HourBinding(is24HourView: Boolean) {
	setIs24HourView(is24HourView)
}