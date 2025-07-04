package com.pickup.sports.ui.home_modules.game_details

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.pickup.sports.data.model.Images


class MyPagerAdapter(private val images: List<Images>) : PagerAdapter() {
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageView = ImageView(container.context).apply {
            setImageResource(images[position].image) // Access imageRes inside Images class
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        container.addView(imageView)
        return imageView
    }

    override fun getCount(): Int = images.size

    override fun isViewFromObject(view: View, obj: Any): Boolean = view == obj

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }
}
