package com.protato.app.ai

import com.protato.app.AgentConversationMessage
import java.util.Locale

internal sealed interface AgentIntent {
    data class Chat(val topic: String?) : AgentIntent
    data class PlanTodos(val rawGoal: String) : AgentIntent
    data class UpdateTodo(val targetHint: String, val changeHint: String) : AgentIntent
    data class MoveOpenTodosToToday(val targetHint: String) : AgentIntent
    data class QueryTodos(val query: String) : AgentIntent
    data class SummarizeRecords(val rangeHint: String?) : AgentIntent
    data class ConfirmPendingAction(val targetHint: String?) : AgentIntent
    data class RejectPendingAction(val targetHint: String?) : AgentIntent
    data class AskClarification(val question: String) : AgentIntent
}

internal data class AgentIntentRequest(
    val userText: String,
    val recentMessages: List<AgentConversationMessage> = emptyList(),
    val hasPendingActions: Boolean = false,
    val nowMillis: Long = System.currentTimeMillis()
)

internal data class AgentIntentResult(
    val intent: AgentIntent,
    val confidence: Float,
    val reason: String
)

internal class AgentIntentResolver {
    fun resolve(request: AgentIntentRequest): AgentIntentResult {
        val text = request.userText.trim()
        val lowered = text.lowercase(Locale.getDefault())
        val recentContext = request.recentMessages
            .takeLast(6)
            .joinToString("\n") { "${it.role}:${it.content}" }
            .lowercase(Locale.getDefault())
        val hasPlanningContext = recentContext.hasAny(planningContextWords)
        if (text.isBlank()) {
            return clarify("用户输入为空")
        }
        if (request.hasPendingActions && lowered.hasAny(confirmWords)) {
            return AgentIntentResult(
                intent = AgentIntent.ConfirmPendingAction(targetHint = null),
                confidence = 0.95f,
                reason = "用户在存在待确认动作时表达确认"
            )
        }
        if (request.hasPendingActions && lowered.hasAny(rejectWords)) {
            return AgentIntentResult(
                intent = AgentIntent.RejectPendingAction(targetHint = null),
                confidence = 0.95f,
                reason = "用户在存在待确认动作时表达拒绝"
            )
        }
        if (!request.hasPendingActions && lowered.hasAny(ambiguousPendingReferences)) {
            return clarify("用户引用了待确认项，但当前没有 pending action")
        }
        if (lowered.hasAny(recordSummaryWords)) {
            return AgentIntentResult(
                intent = AgentIntent.SummarizeRecords(rangeHint = text),
                confidence = 0.82f,
                reason = "用户询问复盘或总结"
            )
        }
        if (lowered.requestsMovingOpenTodosToToday()) {
            return AgentIntentResult(
                intent = AgentIntent.MoveOpenTodosToToday(targetHint = text),
                confidence = 0.86f,
                reason = "用户想把未完成待办安排到今天"
            )
        }
        if (lowered.hasAny(todoQueryWords)) {
            return AgentIntentResult(
                intent = AgentIntent.QueryTodos(query = text),
                confidence = 0.82f,
                reason = "用户查询待办"
            )
        }
        if (lowered.looksLikeTodoUpdateRequest()) {
            return AgentIntentResult(
                intent = AgentIntent.UpdateTodo(targetHint = text, changeHint = text),
                confidence = 0.78f,
                reason = "用户表达修改已有待办"
            )
        }
        if (text.looksLikeTodoPlanningRequest(hasPlanningContext)) {
            return AgentIntentResult(
                intent = AgentIntent.PlanTodos(rawGoal = text),
                confidence = 0.8f,
                reason = "用户表达规划或创建待办"
            )
        }
        return AgentIntentResult(
            intent = AgentIntent.Chat(topic = text),
            confidence = 0.62f,
            reason = "未命中明确命令，作为普通对话"
        )
    }

    private fun clarify(reason: String): AgentIntentResult {
        return AgentIntentResult(
            intent = AgentIntent.AskClarification("我还不能确定你指的是哪一项，可以再说具体一点吗？"),
            confidence = 0.25f,
            reason = reason
        )
    }
}

private val confirmWords = listOf("可以", "确认", "同意", "就这样", "按这个", "按第二个", "ok", "yes")
private val rejectWords = listOf("不要", "取消", "拒绝", "算了", "不是", "不对")
private val ambiguousPendingReferences = listOf("第二个", "第一个", "这个", "那个", "就这样", "按这个", "按第二个")
private val recordSummaryWords = listOf("总结", "复盘", "效率", "记录", "最近表现")
private val planningContextWords = listOf("待办", "任务", "计划", "安排", "规划", "拆解", "整理成", "生成待办", "创建待办")
private val todoQueryWords = listOf(
    "有哪些待办",
    "有什么任务",
    "什么任务",
    "查一下",
    "看看待办",
    "今天还有",
    "今天有什么",
    "未完成",
    "没完成",
    "没有完成",
    "还没做",
    "没做完",
    "已完成",
    "完成了什么",
    "今天做了什么",
    "最近完成了什么",
    "需要做什么",
    "要做什么",
    "该做什么"
)

private fun String.hasAny(words: List<String>): Boolean {
    return words.any { it in this }
}

private fun String.requestsMovingOpenTodosToToday(): Boolean {
    val targetOpenTodos = hasAny(listOf("未完成", "没完成", "还没做", "没有完成", "待办"))
    val toToday = hasAny(listOf("今天", "今日"))
    val moveVerb = hasAny(listOf("改到", "挪到", "移到", "放到", "安排到", "调到", "设为"))
    return targetOpenTodos && toToday && moveVerb
}

private fun String.looksLikeTodoUpdateRequest(): Boolean {
    val updateVerb = hasAny(listOf("修改", "调整", "挪到", "改到", "改成", "延期", "提前", "提醒", "转到"))
    val hasTarget = hasAny(listOf("任务", "待办", "这个", "那个", "日期", "今天", "明天", "后天"))
    return updateVerb && hasTarget
}
