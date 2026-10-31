package com.odtheking.odinaddon.features.impl.skyblock

import com.mojang.blaze3d.platform.InputConstants
import com.odtheking.odin.clickgui.settings.RenderableSetting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.KeybindSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.RenderExtractEvent
import com.odtheking.odin.events.core.CancellableEvent
import com.odtheking.odin.features.Module
import com.odtheking.odin.events.core.on
import com.odtheking.odin.utils.itemId
import com.odtheking.odinaddon.features.impl.skyblock.event.KeyboardEvent
import com.odtheking.odinaddon.features.impl.skyblock.event.MouseEvent
import com.odtheking.odinaddon.mixin.KeyMappingAccessor
import net.minecraft.client.KeyMapping

object Click : Module(
    name = "Auto Clicker",
    description = "Autoclicks left/right click"
) {
    private val lcEnabled by BooleanSetting("Left Click", false, desc = "Toggle Left Click AC")
    private val lcHold by BooleanSetting("Left Click Hold", false, desc = "Hold to Use AC").withDependency { lcEnabled }
    private val lcUse by KeybindSetting(
        "Use LC",
        InputConstants.UNKNOWN,
        desc = "Keybind to Use"
    ).withDependency { lcEnabled }
    private val termOnly by BooleanSetting(
        "Terminator Only",
        false,
        desc = "Left clicks automatically only while using terminator."
    ).withDependency { lcEnabled }
    private val leftCps by NumberSetting(
        "Left Clicks per Second",
        5,
        range = 1..15,
        increment = 1,
        desc = "Number of clicks per second"
    ).withDependency { lcEnabled }

    private val rcEnabled by BooleanSetting("Right Click", false, desc = "Toggle Right Click AC")
    private val rcHold by BooleanSetting(
        "Right Click Hold",
        false,
        desc = "Hold to Use AC"
    ).withDependency { rcEnabled }
    private val rcUse by KeybindSetting(
        "Use RC",
        InputConstants.UNKNOWN,
        desc = "Keybind to Use"
    ).withDependency { rcEnabled }
    private val rightCps by NumberSetting(
        "Right Clicks per Second",
        5,
        range = 1..15,
        increment = 1,
        desc = "Number of clicks per second"
    ).withDependency { rcEnabled }


    private var nextLeftClick = .0
    private var nextRightClick = .0

    private var lastLCState = false;
    private var lastRCState = false;
    private val heldMouseKeys = mutableSetOf<InputConstants.Key>()

    init {
        nextRightClick = 0.0
        nextLeftClick = 0.0
        on<RenderExtractEvent> {
            val nowMillis = System.currentTimeMillis()
            if (mc.gui.screen() != null) {
                heldMouseKeys.clear()
                return@on
            }
            if (!rcEnabled && !lcEnabled) return@on
            if (rcEnabled) tryRightClick(nowMillis)
            if (lcEnabled) tryLeftClick(nowMillis, termOnly)
        }

        on<MouseEvent.Click> {
            val key = InputConstants.Type.MOUSE.getOrCreate(button)
            heldMouseKeys.add(key)
            handleInputEvent(this, key, termOnly);
        }

        on<MouseEvent.Release> {
            heldMouseKeys.remove(InputConstants.Type.MOUSE.getOrCreate(button))
        }

        on<KeyboardEvent> {
            handleInputEvent(this, button, termOnly);
        }
    }

    private fun tryLeftClick(now: Long, termOnly: Boolean = false) {
        if (!termOnly) {
            if ((!lcUse.isDown && lcHold) || (!lcHold && !lastLCState)) return;
        } else if (!(mc.player?.mainHandItem?.itemId == "TERMINATOR" && mc.options.keyUse.isDown)) return

        if (now < nextLeftClick) return;

        val key = (mc.options.keyAttack as KeyMappingAccessor).boundKey;
        nextLeftClick =
            now + ((1000 / leftCps) + ((Math.random() - .5) * 60.0)) + 4 //System.currentMillis() + (1s/cps + random 0-1s - 0.5s * 60s) + 4ms
        KeyMapping.click(key);

    }

    private fun tryRightClick(now: Long) {
        if ((!rcUse.isDown && rcHold) || (!rcHold && !lastRCState)) return;
        if (now < nextRightClick) return;

        val key = (mc.options.keyUse as KeyMappingAccessor).boundKey;
        nextRightClick = now + ((1000 / rightCps) + ((Math.random() - .5) * 60.0)) + 4
        KeyMapping.click(key);
    }

    private fun handleInputEvent(event: CancellableEvent, button: InputConstants.Key, termOnly: Boolean = false) {
        val left = (mc.options.keyAttack as KeyMappingAccessor).boundKey
        val right = (mc.options.keyUse as KeyMappingAccessor).boundKey
        val shouldTerm = mc.player?.mainHandItem?.itemId == "TERMINATOR" && mc.options.keyUse.isDown

        val lcActive = if (termOnly) shouldTerm else lcEnabled && ((lcHold && lcUse.isDown) || (!lcHold && lastLCState))
        val rcActive = rcEnabled && ((rcHold && rcUse.isDown) || (!rcHold && lastRCState))

        if (button == left && lcActive) event.cancel()
        if (button == right && rcActive) event.cancel()

        if (!lcHold && lcEnabled && button == lcUse) lastLCState = !lastLCState
        if (!rcHold && rcEnabled && button == rcUse) lastRCState = !lastRCState
    }


    val InputConstants.Key.isDown: Boolean
        get() {
            return when (type) {
                InputConstants.Type.KEYBOARD -> InputConstants.isKeyDown(value)
                InputConstants.Type.MOUSE -> this in heldMouseKeys
            }
        }

}
