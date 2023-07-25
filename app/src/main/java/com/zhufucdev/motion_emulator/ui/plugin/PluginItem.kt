package com.zhufucdev.motion_emulator.ui.plugin

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.zhufucdev.api.ProductQuery
import com.zhufucdev.motion_emulator.plugin.Plugin
import com.zhufucdev.motion_emulator.plugin.Plugins

@Stable
class PluginItem(
    val id: String,
    val title: String,
    val subtitle: String = "",
    enabled: Boolean,
    state: PluginItemState
) {
    var enabled by mutableStateOf(enabled)
    var state by mutableStateOf(state)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PluginItem

        if (id != other.id) return false
        if (enabled != other.enabled) return false
        return state == other.state
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + enabled.hashCode()
        result = 31 * result + state.hashCode()
        return result
    }
}

enum class PluginItemState {
    NOT_DOWNLOADED, NONE, UPDATE
}

fun Plugin.toPluginItem(enabled: Boolean) = PluginItem(
    id = packageName,
    title = name,
    subtitle = description,
    enabled = enabled,
    state = PluginItemState.NONE
)

fun ProductQuery.toPluginItem() = PluginItem(
    id = category.firstOrNull { Regex.fromLiteral("""([a-zA-Z_-]*\.)+""").matches(it) } ?: key,
    title = name,
    enabled = false,
    state = PluginItemState.NOT_DOWNLOADED
)

fun PluginItem.findPlugin() = Plugins.available.firstOrNull { it.packageName == id }
