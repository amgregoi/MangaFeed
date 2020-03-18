package com.amgregoire.mangafeed.v2.network.api

import com.amgregoire.mangafeed.v2.network.RetroService

/**
 * Created by amgregoi on 3/20/19.
 */

interface FableAPI
{
    companion object
    {
        fun getInstance(): FableAPI = RetroService.retrofit.create(FableAPI::class.java)
    }
}