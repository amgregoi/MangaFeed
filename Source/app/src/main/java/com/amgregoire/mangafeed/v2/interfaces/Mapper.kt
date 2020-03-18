package com.amgregoire.mangafeed.v2.interfaces

interface Mapper<I, O>
{
    fun map(input: I): O
}