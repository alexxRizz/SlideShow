package ru.rizz.slideshow.common

import android.view.*
import androidx.databinding.*

//@BindingAdapter("android:src")
//fun ImageView.setSrcBinding(@DrawableRes resId: Int) =
//	setImageResource(resId)

@BindingAdapter("android:visibility")
fun View.setVisibilityBinding(isVisible: Boolean) {
	visibility = if (isVisible) View.VISIBLE else View.GONE
}