package org.aetherfeed.app.news

fun canFetchNews(wifiOnly: Boolean, unmetered: Boolean): Boolean = !wifiOnly || unmetered
