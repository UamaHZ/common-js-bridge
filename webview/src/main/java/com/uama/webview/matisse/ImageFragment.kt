package com.uama.webview.matisse

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.uama.webview.R
import kotlinx.android.synthetic.main.fragment_image_preview.*

/**
 *Author:ruchao.jiang
 *Created: 2019/4/1 15:40
 *Email:ruchao.jiang@uama.com.cn
 */
class ImageFragment : Fragment(){
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_image_preview,container,false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fragmentStart()
    }

    companion object {
        const val ARG_URL = "URL"
        fun getInstance(url:String):ImageFragment{
            val fragment = ImageFragment()
            val bundle = Bundle()
            bundle.putString(ARG_URL,url)
            fragment.arguments = bundle
            return fragment
        }
    }


    private fun fragmentStart() {
        arguments?.let {
            Glide.with(context!!).load(it.getString(ARG_URL)).into(itemImage)
        }
    }
}