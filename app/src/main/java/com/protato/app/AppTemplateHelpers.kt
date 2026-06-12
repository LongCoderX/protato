package com.protato.app

import com.protato.app.util.newId

internal fun emptyTemplate(): RecordTemplate {
    return RecordTemplate(
        id = newId(),
        name = "新模板",
        fields = emptyList()
    )
}
