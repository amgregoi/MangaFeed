package com.amgregoire.mangafeed.v2.ui.settings

import android.os.Bundle
import android.support.v7.widget.DrawableUtils
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.v2.NavigationType
import com.amgregoire.mangafeed.v2.ResourceFactory
import com.amgregoire.mangafeed.v2.ui.base.BaseFragment
import com.amgregoire.mangafeed.v2.ui.map.ToolbarMap
import kotlinx.android.synthetic.main.fragment_sync_sources.view.*

class SyncSourceFragment : BaseFragment()
{
    private var isSyncing = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        self = inflater.inflate(R.layout.fragment_sync_sources, null)
        return self
    }

    override fun updateParentSettings()
    {
        val parent = activity ?: return
        (parent as ToolbarMap).setTitle(getString(R.string.sources))
        (parent as ToolbarMap).setOptionsMenu(R.menu.menu_empty)
        (parent as ToolbarMap).setNavigationIcon(ResourceFactory.getNavigationIcon(NavigationType.Back))
    }

    override fun onStart()
    {
        super.onStart()
        self.rvSync.layoutManager = LinearLayoutManager(context)
        self.rvSync.adapter = SyncAdapter()

        self.buttonSync.setClickListener(View.OnClickListener {
            if (isSyncing) stopSync()
            else startSync()
        })
    }

    private fun startSync()
    {
        self.buttonSync.setButtonText(getString(R.string.text_stop_sync))
        isSyncing = true
        val count = (self.rvSync.layoutManager as LinearLayoutManager).itemCount
        var completeCount = 0
        for (i in 0..count)
        {
            val holder = (self.rvSync.findViewHolderForAdapterPosition(i) as? SyncAdapter.SyncViewHolder)
            holder?.startSync {
                completeCount++
                if (count == completeCount) stopSync()
            }
        }
    }

    private fun stopSync()
    {
        self.buttonSync.setButtonText(getString(R.string.text_start_sync))
        isSyncing = false
        (self.rvSync.adapter as SyncAdapter).stopSync()
    }

    companion object
    {
        val TAG: String = SyncSourceFragment::class.simpleName!!
        fun newInstance() = SyncSourceFragment()
    }
}