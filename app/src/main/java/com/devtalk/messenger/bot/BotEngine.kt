package com.devtalk.messenger.bot

import com.devtalk.messenger.data.model.*

/**
 * Processes incoming messages against a bot's rules and returns a response.
 * Stateless engine — each call is independent.
 */
object BotEngine {

    fun processMessage(bot: Bot, incomingText: String, isFirstMessage: Boolean = false): String? {
        val text = incomingText.trim()

        // Priority 1: /start or first message
        if (isFirstMessage || text.equals("/start", ignoreCase = true)) {
            return bot.welcomeMessage.ifBlank { null }
        }

        // Priority 2: Check rules in order
        for (rule in bot.rules.filter { it.isEnabled }) {
            val matched = when (rule.triggerType) {
                TriggerType.COMMAND -> {
                    val cmd = text.removePrefix("/").split(" ").firstOrNull()?.lowercase()
                    cmd == rule.triggerValue.lowercase()
                }
                TriggerType.KEYWORD -> {
                    text.equals(rule.triggerValue, ignoreCase = true)
                }
                TriggerType.CONTAINS -> {
                    text.contains(rule.triggerValue, ignoreCase = true)
                }
                TriggerType.ANY -> true
                TriggerType.START -> isFirstMessage
            }

            if (matched && rule.responses.isNotEmpty()) {
                return pickResponse(rule)
            }
        }

        // Fallback
        return bot.fallbackMessage.ifBlank { null }
    }

    private fun pickResponse(rule: BotRule): String {
        return when (rule.responseType) {
            ResponseType.TEXT -> rule.responses.firstOrNull() ?: ""
            ResponseType.RANDOM -> rule.responses.random()
            ResponseType.SEQUENCE -> {
                // Simple sequence: use hash of current minute to cycle through
                val idx = ((System.currentTimeMillis() / 60000) % rule.responses.size).toInt()
                rule.responses[idx]
            }
        }
    }

    fun getCommandList(bot: Bot): List<Pair<String, String>> {
        return bot.rules
            .filter { it.isEnabled && it.triggerType == TriggerType.COMMAND }
            .map { rule ->
                val preview = when (rule.responseType) {
                    ResponseType.TEXT -> rule.responses.firstOrNull()?.take(50) ?: ""
                    ResponseType.RANDOM -> "${rule.responses.size} responses"
                    ResponseType.SEQUENCE -> "${rule.responses.size} in sequence"
                }
                "/${rule.triggerValue}" to preview
            }
    }

    fun validateBot(bot: Bot): List<String> {
        val errors = mutableListOf<String>()
        if (bot.name.isBlank()) errors.add("Bot needs a name")
        if (bot.name.length < 3) errors.add("Name must be 3+ characters")
        if (bot.rules.isEmpty()) errors.add("Add at least one rule")
        bot.rules.forEach { rule ->
            if (rule.triggerType != TriggerType.ANY && rule.triggerType != TriggerType.START
                && rule.triggerValue.isBlank()) {
                errors.add("Rule trigger value is empty")
            }
            if (rule.responses.isEmpty()) {
                errors.add("Rule '${rule.getDisplayTrigger()}' has no responses")
            }
        }
        return errors
    }
}
