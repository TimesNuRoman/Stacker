package com.devtalk.messenger.data.model

data class Bot(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val creatorUid: String = "",
    val creatorName: String = "",
    val avatarEmoji: String = "🤖",
    val isPublic: Boolean = true,
    val templateId: String = "",
    val rules: List<BotRule> = emptyList(),
    val welcomeMessage: String = "",
    val fallbackMessage: String = "I don't understand. Type /help to see commands.",
    val usageCount: Int = 0,
    val addedByCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val tags: List<String> = emptyList()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "name" to name,
        "description" to description,
        "creatorUid" to creatorUid,
        "creatorName" to creatorName,
        "avatarEmoji" to avatarEmoji,
        "isPublic" to isPublic,
        "templateId" to templateId,
        "rules" to rules.map { it.toMap() },
        "welcomeMessage" to welcomeMessage,
        "fallbackMessage" to fallbackMessage,
        "usageCount" to usageCount,
        "addedByCount" to addedByCount,
        "createdAt" to createdAt,
        "tags" to tags
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): Bot = Bot(
            id = map["id"] as? String ?: "",
            name = map["name"] as? String ?: "",
            description = map["description"] as? String ?: "",
            creatorUid = map["creatorUid"] as? String ?: "",
            creatorName = map["creatorName"] as? String ?: "",
            avatarEmoji = map["avatarEmoji"] as? String ?: "🤖",
            isPublic = map["isPublic"] as? Boolean ?: true,
            templateId = map["templateId"] as? String ?: "",
            rules = (map["rules"] as? List<*>)?.mapNotNull { item ->
                @Suppress("UNCHECKED_CAST")
                (item as? Map<String, Any?>)?.let { BotRule.fromMap(it) }
            } ?: emptyList(),
            welcomeMessage = map["welcomeMessage"] as? String ?: "",
            fallbackMessage = map["fallbackMessage"] as? String ?: "I don't understand.",
            usageCount = (map["usageCount"] as? Number)?.toInt() ?: 0,
            addedByCount = (map["addedByCount"] as? Number)?.toInt() ?: 0,
            createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            tags = (map["tags"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
        )
    }
}

data class BotRule(
    val id: String = "",
    val triggerType: TriggerType = TriggerType.KEYWORD,
    val triggerValue: String = "",
    val responseType: ResponseType = ResponseType.TEXT,
    val responses: List<String> = emptyList(),
    val isEnabled: Boolean = true
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "triggerType" to triggerType.name,
        "triggerValue" to triggerValue,
        "responseType" to responseType.name,
        "responses" to responses,
        "isEnabled" to isEnabled
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): BotRule = BotRule(
            id = map["id"] as? String ?: "",
            triggerType = try {
                TriggerType.valueOf(map["triggerType"] as? String ?: "KEYWORD")
            } catch (_: Exception) { TriggerType.KEYWORD },
            triggerValue = map["triggerValue"] as? String ?: "",
            responseType = try {
                ResponseType.valueOf(map["responseType"] as? String ?: "TEXT")
            } catch (_: Exception) { ResponseType.TEXT },
            responses = (map["responses"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            isEnabled = map["isEnabled"] as? Boolean ?: true
        )
    }

    fun getDisplayTrigger(): String = when (triggerType) {
        TriggerType.COMMAND -> "/$triggerValue"
        TriggerType.KEYWORD -> "\"$triggerValue\""
        TriggerType.CONTAINS -> "*$triggerValue*"
        TriggerType.ANY -> "any message"
        TriggerType.START -> "/start"
    }
}

enum class TriggerType {
    COMMAND,   // /command
    KEYWORD,   // exact match
    CONTAINS,  // message contains text
    ANY,       // any message
    START;     // first message / /start

    fun label(): String = when (this) {
        COMMAND -> "Command (/cmd)"
        KEYWORD -> "Exact word"
        CONTAINS -> "Contains text"
        ANY -> "Any message"
        START -> "First message"
    }

    fun hint(): String = when (this) {
        COMMAND -> "hello (user types /hello)"
        KEYWORD -> "hello"
        CONTAINS -> "hello (matches: hello world)"
        ANY -> ""
        START -> ""
    }
}

enum class ResponseType {
    TEXT,      // single response
    RANDOM,    // random from list
    SEQUENCE;  // one after another

    fun label(): String = when (this) {
        TEXT -> "Fixed text"
        RANDOM -> "Random from list"
        SEQUENCE -> "One after another"
    }
}

// Pre-built templates for instant bot creation
data class BotTemplate(
    val id: String,
    val name: String,
    val emoji: String,
    val description: String,
    val category: String,
    val presetRules: List<BotRule>,
    val presetWelcome: String,
    val presetFallback: String,
    val presetTags: List<String>
)

object BotTemplates {
    val all = listOf(
        BotTemplate(
            id = "greeter",
            name = "Greeter Bot",
            emoji = "👋",
            description = "Greets users and answers basic questions",
            category = "Basic",
            presetWelcome = "Hey there! I'm a friendly bot. Type /help to see what I can do!",
            presetFallback = "Hmm, I don't know that one. Try /help!",
            presetTags = listOf("greeting", "basic", "starter"),
            presetRules = listOf(
                BotRule("r1", TriggerType.COMMAND, "help", ResponseType.TEXT,
                    listOf("Available commands:\n/hello — Say hi\n/joke — Get a joke\n/about — About me")),
                BotRule("r2", TriggerType.COMMAND, "hello", ResponseType.RANDOM,
                    listOf("Hey! 👋", "Hello there!", "Hi, friend!", "What's up! 🤙")),
                BotRule("r3", TriggerType.COMMAND, "joke", ResponseType.RANDOM,
                    listOf(
                        "Why do programmers prefer dark mode? Because light attracts bugs! 🐛",
                        "There are 10 types of people: those who understand binary and those who don't.",
                        "A SQL query walks into a bar, sees two tables, and asks: 'Can I JOIN you?'",
                        "!false — It's funny because it's true."
                    )),
                BotRule("r4", TriggerType.CONTAINS, "hi", ResponseType.RANDOM,
                    listOf("Hey! 👋", "Hello!", "What's up?")),
                BotRule("r5", TriggerType.CONTAINS, "thanks", ResponseType.RANDOM,
                    listOf("You're welcome! 😊", "No problem!", "Anytime! 👍"))
            )
        ),
        BotTemplate(
            id = "8ball",
            name = "Magic 8-Ball",
            emoji = "🎱",
            description = "Ask any yes/no question, get a mystical answer",
            category = "Fun",
            presetWelcome = "🎱 I am the Magic 8-Ball. Ask me any yes/no question!",
            presetFallback = "",
            presetTags = listOf("fun", "game", "8ball", "fortune"),
            presetRules = listOf(
                BotRule("r1", TriggerType.COMMAND, "help", ResponseType.TEXT,
                    listOf("Just type any question and I'll answer! 🎱")),
                BotRule("r2", TriggerType.ANY, "", ResponseType.RANDOM,
                    listOf(
                        "🎱 It is certain.", "🎱 Without a doubt.",
                        "🎱 Yes, definitely.", "🎱 You may rely on it.",
                        "🎱 Most likely.", "🎱 Outlook good.",
                        "🎱 Signs point to yes.", "🎱 Yes.",
                        "🎱 Reply hazy, try again.", "🎱 Ask again later.",
                        "🎱 Better not tell you now.", "🎱 Cannot predict now.",
                        "🎱 Don't count on it.", "🎱 My reply is no.",
                        "🎱 My sources say no.", "🎱 Outlook not so good.",
                        "🎱 Very doubtful."
                    ))
            )
        ),
        BotTemplate(
            id = "quiz",
            name = "Quiz Master",
            emoji = "🧠",
            description = "Ask trivia questions with /quiz",
            category = "Fun",
            presetWelcome = "🧠 Welcome to Quiz Master! Type /quiz for a question!",
            presetFallback = "Type /quiz to get a question!",
            presetTags = listOf("quiz", "trivia", "game", "fun"),
            presetRules = listOf(
                BotRule("r1", TriggerType.COMMAND, "help", ResponseType.TEXT,
                    listOf("Commands:\n/quiz — Get a random question\n/score — Your score (coming soon)")),
                BotRule("r2", TriggerType.COMMAND, "quiz", ResponseType.RANDOM,
                    listOf(
                        "🧠 What does HTTP stand for?\n→ HyperText Transfer Protocol",
                        "🧠 What year was Git created?\n→ 2005, by Linus Torvalds",
                        "🧠 What does SQL stand for?\n→ Structured Query Language",
                        "🧠 What port does HTTPS use?\n→ 443",
                        "🧠 What does API stand for?\n→ Application Programming Interface",
                        "🧠 What language is Android built with?\n→ Kotlin (and Java)",
                        "🧠 What does CSS stand for?\n→ Cascading Style Sheets",
                        "🧠 Who created Linux?\n→ Linus Torvalds, in 1991"
                    ))
            )
        ),
        BotTemplate(
            id = "motivator",
            name = "Motivation Bot",
            emoji = "💪",
            description = "Get inspired with motivational quotes",
            category = "Utility",
            presetWelcome = "💪 I'm here to keep you motivated! Type /quote for wisdom.",
            presetFallback = "You got this! Type /quote for a motivational boost!",
            presetTags = listOf("motivation", "quotes", "inspiration"),
            presetRules = listOf(
                BotRule("r1", TriggerType.COMMAND, "help", ResponseType.TEXT,
                    listOf("/quote — Random motivational quote\n/daily — Quote of the day")),
                BotRule("r2", TriggerType.COMMAND, "quote", ResponseType.RANDOM,
                    listOf(
                        "\"The only way to do great work is to love what you do.\" — Steve Jobs",
                        "\"Code is like humor. When you have to explain it, it's bad.\" — Cory House",
                        "\"First, solve the problem. Then, write the code.\" — John Johnson",
                        "\"Talk is cheap. Show me the code.\" — Linus Torvalds",
                        "\"Any fool can write code that a computer can understand. Good programmers write code that humans can understand.\" — Martin Fowler",
                        "\"The best time to plant a tree was 20 years ago. The second best time is now.\"",
                        "\"It does not matter how slowly you go as long as you do not stop.\" — Confucius",
                        "\"Done is better than perfect.\" — Sheryl Sandberg"
                    )),
                BotRule("r3", TriggerType.COMMAND, "daily", ResponseType.RANDOM,
                    listOf(
                        "Today's wisdom: Ship it. You can always iterate.",
                        "Today's wisdom: Every expert was once a beginner.",
                        "Today's wisdom: Debug your mind before debugging code."
                    ))
            )
        ),
        BotTemplate(
            id = "dice",
            name = "Dice & Random",
            emoji = "🎲",
            description = "Roll dice, flip coins, pick random numbers",
            category = "Fun",
            presetWelcome = "🎲 I generate random stuff! Try /roll, /flip, or /pick",
            presetFallback = "Try /roll, /flip, or /pick !",
            presetTags = listOf("random", "dice", "coin", "game"),
            presetRules = listOf(
                BotRule("r1", TriggerType.COMMAND, "help", ResponseType.TEXT,
                    listOf("/roll — Roll a d6\n/flip — Flip a coin\n/pick — Random 1-100")),
                BotRule("r2", TriggerType.COMMAND, "roll", ResponseType.RANDOM,
                    listOf("🎲 You rolled: 1", "🎲 You rolled: 2", "🎲 You rolled: 3",
                        "🎲 You rolled: 4", "🎲 You rolled: 5", "🎲 You rolled: 6")),
                BotRule("r3", TriggerType.COMMAND, "flip", ResponseType.RANDOM,
                    listOf("🪙 Heads!", "🪙 Tails!")),
                BotRule("r4", TriggerType.COMMAND, "pick", ResponseType.RANDOM,
                    (1..20).map { "🎯 Random number: ${(1..100).random()}" })
            )
        ),
        BotTemplate(
            id = "faq",
            name = "FAQ Bot",
            emoji = "❓",
            description = "Answer frequently asked questions (you fill in the Q&A)",
            category = "Utility",
            presetWelcome = "❓ Hi! I can answer your questions. Type /help to see topics.",
            presetFallback = "I don't have an answer for that yet. Try /help to see available topics.",
            presetTags = listOf("faq", "help", "support", "info"),
            presetRules = listOf(
                BotRule("r1", TriggerType.COMMAND, "help", ResponseType.TEXT,
                    listOf("Available topics:\n/q1 — Question 1\n/q2 — Question 2\n\n(Edit these in the bot builder!)")),
                BotRule("r2", TriggerType.COMMAND, "q1", ResponseType.TEXT,
                    listOf("Answer to question 1 goes here. Edit me!")),
                BotRule("r3", TriggerType.COMMAND, "q2", ResponseType.TEXT,
                    listOf("Answer to question 2 goes here. Edit me!"))
            )
        ),
        BotTemplate(
            id = "empty",
            name = "Blank Bot",
            emoji = "🔧",
            description = "Start from scratch — build your own rules",
            category = "Advanced",
            presetWelcome = "Hello! I'm a custom bot.",
            presetFallback = "I don't understand that yet.",
            presetTags = listOf("custom"),
            presetRules = listOf(
                BotRule("r1", TriggerType.COMMAND, "help", ResponseType.TEXT,
                    listOf("No commands configured yet."))
            )
        )
    )

    fun getById(id: String): BotTemplate? = all.find { it.id == id }
    val categories: List<String> get() = all.map { it.category }.distinct()
}
