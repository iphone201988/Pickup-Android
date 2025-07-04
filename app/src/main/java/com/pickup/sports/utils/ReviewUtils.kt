package com.pickup.sports.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.google.android.play.core.review.ReviewManagerFactory

object ReviewUtils {

    // Launch the in-app review flow
    fun launchInAppReview(activity: Activity) {
        val manager = ReviewManagerFactory.create(activity)  // Correct usage
        val request = manager.requestReviewFlow()

        request.addOnCompleteListener { task ->
            Log.i("Review", "Review flow successful")
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(activity, reviewInfo)
                flow.addOnCompleteListener {
                    // Optionally handle completion of the review flow if needed
                    Log.i("Review", "Review flow completed")
                }
            } else {
                Log.e("Review", "In-app review failed", task.exception)
                // Fallback: If in-app review fails, open Play Store for review
                openPlayStoreForReview(activity)
            }
        }
    }

    // Fallback: Opens the Play Store page for the app
    private fun openPlayStoreForReview(context: Context) {
        val packageName = context.packageName
        val uri = Uri.parse("market://details?id=$packageName")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK)

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // If Google Play Store app is not available, fallback to browser
            context.startActivity(
                Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
            )
        }
    }
}