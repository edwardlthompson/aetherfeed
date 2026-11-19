package org.aetherfeed.shared.ui.insets

import androidx.compose.ui.Modifier

/** Platform insets land as expect/actual; Android app keeps SystemBars until cutover. */
fun Modifier.bottomInsetPadding(): Modifier = this
