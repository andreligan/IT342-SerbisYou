package com.example.serbisyo_it342_g3.utils

import android.content.Context
import android.util.AttributeSet
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

/**
 * Custom SwipeRefreshLayout class to resolve reference issues
 */
class CustomSwipeRefreshLayout : SwipeRefreshLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
} 