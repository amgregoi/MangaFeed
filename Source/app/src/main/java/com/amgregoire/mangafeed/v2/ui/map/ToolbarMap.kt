package com.amgregoire.mangafeed.v2.ui.map

interface ToolbarMap
{
    fun setTitle(title:String)

    fun showSpinner()

    fun setNavigationIcon(iconResource:Int?)

    fun setToolbarColor(color: Int)

    fun hideToolbar()

    fun setToolbarVisibility(visibility: Int)

    fun showToolbar()

    fun showToolbarElevation()

    fun hideToolbarElevation()

    /***
     * 0 -> Default (no menu)
     * R.menu.<insert menu>
    </insert> */
    fun setOptionsMenu(menuId: Int?)
}