package com.amgregoire.mangafeed.v2.ui.catalog.fragment

import androidx.lifecycle.Observer
import androidx.fragment.app.Fragment
import com.amgregoire.mangafeed.R
import kotlinx.android.synthetic.main.fragment_catalog.view.*

class RecentFragment : CatalogBase()
{
    override fun onStart()
    {
        super.onStart()

        self.swipeManga.isEnabled = true
        self.swipeManga.setDistanceToTriggerSync(400)

        catalogViewModel?.recent?.observe(this, Observer { mangaList ->
            mangaList ?: return@Observer

            state =
                    if (mangaList.isEmpty()) State.Failed(Error(getString(R.string.all_no_manga_message)))
                    else State.Complete(mangaList)
        })

        self.swipeManga.setOnRefreshListener {
            self.swipeManga.isRefreshing = false
            state = State.Loading
            catalogViewModel?.retrieveRecentList()
        }
    }

    companion object
    {
        fun newInstance(): androidx.fragment.app.Fragment = RecentFragment()
    }
}