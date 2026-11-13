package org.aetherfeed.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class AetherFeedUiTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun opensSettingsPanelWithThemeAndUpdateControls() {
        composeTestRule.onNodeWithContentDescription("Settings").performClick()
        composeTestRule.onNodeWithTag("settings-heading").assertIsDisplayed()
        composeTestRule.onNodeWithText("Theme").assertIsDisplayed()
        composeTestRule.onNodeWithText("Preferred opener").assertIsDisplayed()
        composeTestRule.onNodeWithText("Article images").assertIsDisplayed()
        composeTestRule.onNodeWithText("Check for updates").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dark theme").performClick()
        composeTestRule.onNodeWithText("Close settings").performClick()
    }

    @Test
    fun opensAboutFromSettings() {
        composeTestRule.onNodeWithContentDescription("Settings").performClick()
        composeTestRule.onNodeWithTag("settings-about").performClick()
        composeTestRule.onNodeWithTag("about-panel").assertIsDisplayed()
        composeTestRule.onNodeWithText("About").assertIsDisplayed()
        composeTestRule.onNodeWithText("Installed format: apk").assertIsDisplayed()
    }

    @Test
    fun switchesModuleTabs() {
        composeTestRule.onNodeWithContentDescription("News tab").performClick()
        composeTestRule.onNodeWithText("News").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Podcasts tab").performClick()
        composeTestRule.onNodeWithText("Podcasts").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Booru tab").performClick()
        composeTestRule.onNodeWithText("Booru").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Settings").performClick()
        composeTestRule.onNodeWithTag("settings-heading").assertIsDisplayed()
    }
}
