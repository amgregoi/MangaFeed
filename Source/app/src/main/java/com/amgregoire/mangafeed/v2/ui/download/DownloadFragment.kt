package com.amgregoire.mangafeed.v2.ui.download

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.v2.ui.base.BaseFragment

class DownloadFragment :BaseFragment()
{
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        self = inflater.inflate(R.layout.fragment_downloads, null)
        return self
    }

    companion object
    {
        fun newInstance() = DownloadFragment()
    }
}