package org.aetherfeed.app.news

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.readerDataStore by preferencesDataStore(name = "reader_prefs")

class ReaderPrefs(private val context: Context) {
    private val targetKey = stringPreferencesKey("article_target")
    private val scaleKey = floatPreferencesKey("body_scale")
    private val policyKey = stringPreferencesKey("image_policy")

    val settings: Flow<ReaderSettings> =
        context.readerDataStore.data.map { prefs ->
            ReaderSettings(
                target = ArticleTarget.parse(prefs[targetKey]),
                bodyScale = ReaderScale.clamp(prefs[scaleKey] ?: ReaderScale.DEFAULT),
                imagePolicy = ImagePolicy.parse(prefs[policyKey]),
            )
        }

    suspend fun setTarget(value: ArticleTarget) {
        context.readerDataStore.edit { it[targetKey] = value.name }
    }

    suspend fun setBodyScale(value: Float) {
        context.readerDataStore.edit { it[scaleKey] = ReaderScale.clamp(value) }
    }

    suspend fun setImagePolicy(value: ImagePolicy) {
        context.readerDataStore.edit { it[policyKey] = value.name }
    }
}
