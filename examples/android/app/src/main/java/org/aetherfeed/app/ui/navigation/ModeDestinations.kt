package org.aetherfeed.app.ui.navigation

import org.aetherfeed.app.R
import org.aetherfeed.app.domain.Feed
import org.aetherfeed.app.domain.ModuleKind

const val BOARDS_EMPTY_TEST_TAG = "boards-empty"

fun AppDestination.isModeDestination(): Boolean =
    this == AppDestination.News ||
        this == AppDestination.Podcasts ||
        this == AppDestination.Booru

fun modeTabDestinations(): List<AppDestination> =
    AppDestination.entries.filter { it.isModeDestination() }

fun parseAppDestination(raw: String?): AppDestination =
    AppDestination.entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) }
        ?: AppDestination.News

fun countBoardSources(feeds: Iterable<Feed>): Int =
    feeds.count { it.kind == ModuleKind.Booru }

fun shouldShowBoardsEmpty(sourceCount: Int): Boolean = sourceCount <= 0

fun boardsEmptyBodyRes(): Int = R.string.pane_booru_body
