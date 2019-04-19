package com.uama.webview.matisse

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import com.uama.webview.PreViewBean
import com.uama.webview.R
import kotlinx.android.synthetic.main.activity_image_preview.*

/**
 *Author:ruchao.jiang
 *Created: 2019/4/1 14:54
 *Email:ruchao.jiang@uama.com.cn
 */
public class ImagePreViewActivity : FragmentActivity() {
    val fragmentList: MutableList<Fragment> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_preview)
        start()
    }

    private fun start() {
        val bean = intent.getSerializableExtra("bean") as PreViewBean?
        bean?.let {
            val pics: MutableList<String>? = it.imageUrls
            pics?.forEach {str->
                fragmentList.add(ImageFragment.getInstance(str))
            }

            val adapter =object: FragmentPagerAdapter(supportFragmentManager){
                override fun getCount(): Int=fragmentList.size
                override fun getPageTitle(position: Int): CharSequence? =""
                override fun getItem(position: Int): Fragment=fragmentList[position]
            }
            pager.adapter = adapter
            tx_title.text =String.format("%d/%d",it.currentIndex?.plus(1),fragmentList.size)

            pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) = Unit
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit
                override fun onPageSelected(position: Int) {
                    tx_title.text =String.format("%d/%d",position + 1,fragmentList.size)
                }
            })
        }
    }
}