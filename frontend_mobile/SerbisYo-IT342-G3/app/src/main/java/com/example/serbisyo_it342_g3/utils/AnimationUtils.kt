package com.example.serbisyo_it342_g3.utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.ViewGroup
import com.example.serbisyo_it342_g3.R

/**
 * Utility class to handle animations throughout the app
 */
object AnimationUtils {

    /**
     * Apply login screen animations to the views
     * @param rootView The root view containing all elements to animate
     */
    fun applyLoginAnimations(rootView: View) {
        // Find all tagged views that need animation
        val loginCard = rootView.findViewWithTag<View>("loginCard")
        val usernameInput = rootView.findViewWithTag<View>("username")
        val passwordInput = rootView.findViewWithTag<View>("password")
        val loginButton = rootView.findViewWithTag<View>("loginButton")
        val divider = rootView.findViewWithTag<View>("divider")
        val googleButton = rootView.findViewWithTag<View>("googleButton")
        val signUp = rootView.findViewWithTag<View>("signUp")
        
        // Load animations
        val slideUp = AnimationUtils.loadAnimation(rootView.context, R.anim.slide_up)
        val fadeIn = AnimationUtils.loadAnimation(rootView.context, R.anim.fade_in)
        val scaleUp = AnimationUtils.loadAnimation(rootView.context, R.anim.scale_up)
        
        // Set up card animation
        loginCard?.startAnimation(slideUp)
        
        // Set up form elements animation with delays
        usernameInput?.apply {
            alpha = 0f
            postDelayed({
                alpha = 1f
                startAnimation(fadeIn)
            }, 300)
        }
        
        passwordInput?.apply {
            alpha = 0f
            postDelayed({
                alpha = 1f
                startAnimation(fadeIn)
            }, 400)
        }
        
        loginButton?.apply {
            alpha = 0f
            postDelayed({
                alpha = 1f
                startAnimation(scaleUp)
            }, 500)
        }
        
        // Animate the divider and other elements
        divider?.apply {
            alpha = 0f
            postDelayed({
                alpha = 1f
                startAnimation(fadeIn)
            }, 600)
        }
        
        googleButton?.apply {
            alpha = 0f
            postDelayed({
                alpha = 1f
                startAnimation(scaleUp)
            }, 700)
        }
        
        signUp?.apply {
            alpha = 0f
            postDelayed({
                alpha = 1f
                startAnimation(fadeIn)
            }, 800)
        }
    }
    
    /**
     * Apply a simple button press animation
     * @param view The button view to animate
     */
    fun applyButtonPressAnimation(view: View) {
        val scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 0.9f)
        val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 0.9f)
        scaleDownX.duration = 100
        scaleDownY.duration = 100
        
        val scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 0.9f, 1.0f)
        val scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 0.9f, 1.0f)
        scaleUpX.duration = 100
        scaleUpY.duration = 100
        
        val scaleDown = AnimatorSet()
        scaleDown.play(scaleDownX).with(scaleDownY)
        scaleDown.interpolator = AccelerateDecelerateInterpolator()
        
        val scaleUp = AnimatorSet()
        scaleUp.play(scaleUpX).with(scaleUpY)
        scaleUp.interpolator = AccelerateDecelerateInterpolator()
        
        scaleDown.start()
        
        view.postDelayed({
            scaleUp.start()
        }, 100)
    }
} 