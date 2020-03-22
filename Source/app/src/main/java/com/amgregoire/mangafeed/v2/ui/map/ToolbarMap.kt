package com.amgregoire.mangafeed.v2.ui.map


interface ToolbarSpinnerMap
{
    fun showSpinner()
}

interface ToolbarSearchViewMap
{
    fun showSearchView()
}
interface ToolbarMap : ToolbarSpinnerMap, ToolbarSearchViewMap
{
    fun setTitle(title:String)

    fun setNavigationIcon(iconResource:Int?)

    fun setToolbarColor(color: Int)

    fun hideToolbar()

    fun setToolbarVisibility(visibility: Int)

    fun showToolbar()

    /***
     * 0 -> Default (no menu)
     * R.menu.<insert menu>
    </insert> */
    fun setOptionsMenu(menuId: Int?)
}