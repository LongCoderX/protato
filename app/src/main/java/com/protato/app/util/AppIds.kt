package com.protato.app.util

import java.util.UUID

internal fun newId(): String = UUID.randomUUID().toString()
