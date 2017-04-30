package com.wearefrancis.auth

const val NAME_MAX_LENGTH = 50
const val NAME_MIN_LENGTH = 3
const val USERNAME_REGEX = "^[A-z0-9_]{$NAME_MIN_LENGTH,$NAME_MAX_LENGTH}$"
const val UUID_REGEX = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"