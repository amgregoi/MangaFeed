package com.amgregoire.mangafeed.v2.ui.read

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.v2.enums.ReaderSettings
import com.amgregoire.mangafeed.v2.ui.base.BaseFragment
import com.amgregoire.mangafeed.v2.ui.read.adapter.ImageListAdapter
import com.amgregoire.mangafeed.v2.widget.MangaImageView
import kotlinx.android.synthetic.main.fragment_image2.view.*

class ImageFragment : BaseFragment() {

    private val readerViewModel: ReaderViewModel? by lazy {
        val parent = activity ?: return@lazy null
        ViewModelProvider(parent).get(ReaderViewModel::class.java)
    }

    private val url by lazy { arguments!![URL_KEY] as String }

    private val readerSettings: ReaderSettings
        get() = readerViewModel?.getReaderSetting() ?: ReaderSettings.Max

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_image2, null).also {
            self = it
        }
    }

    override fun onStart() {
        super.onStart()

        setupImages()
        self.emptyState.setButtonClickListener(View.OnClickListener {
            setupImages()
        })

        activity?.let {
            readerViewModel?.readerSettings?.observe(it, Observer {
                setupImages()
            })
        }
    }

    private fun setupImages() {
        self.emptyState.showLoader()
        self.rv.layoutManager = LinearLayoutManager(context)
        self.rv.adapter = ImageListAdapter(
                url = url,
                readerSettings = readerSettings,
                screenListener = screenListener,
                onCompleteListener = { isComplete ->
                    if (isComplete) self.emptyState.hide()
                    else self.emptyState.show()
                })
    }

    private val screenListener = object : MangaImageView.ScreenInteraction {
        override fun goForward() {
            readerViewModel?.incrementPage()
        }

        override fun goBackward() {
            readerViewModel?.decrementPage()
        }

        override fun showToolbar() {
            readerViewModel?.toggleUiState()
        }
    }

    companion object {
        val TAG: String = ImageFragment::class.java.simpleName
        val URL_KEY = "$TAG:URL"
        fun newInstance(url: String) = ImageFragment().apply {
            arguments = Bundle().apply {
                putString(URL_KEY, url)
            }
        }
    }
}