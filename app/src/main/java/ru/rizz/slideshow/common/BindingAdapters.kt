package ru.rizz.slideshow.common

import android.net.*
import android.view.*
import android.widget.*
import androidx.databinding.*

//@BindingAdapter("android:src")
//fun ImageView.setSrcBinding(@DrawableRes resId: Int) =
//	setImageResource(resId)

//@BindingAdapter("android:src")
//fun ImageView.setSrcBinding(uri: Uri?) {
//	if (uri != null)
//		setImageURI(uri)
//}

@BindingAdapter("android:visibility")
fun View.setVisibilityBinding(isVisible: Boolean) {
	visibility = if (isVisible) View.VISIBLE else View.GONE
}