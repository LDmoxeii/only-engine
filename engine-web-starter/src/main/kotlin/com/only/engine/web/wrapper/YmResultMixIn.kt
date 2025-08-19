package com.only.engine.web.wrapper

import com.fasterxml.jackson.annotation.JsonIgnore

abstract class YmResultMixIn {

    @get:JsonIgnore
    abstract val desc: String

    @get:JsonIgnore
    abstract val name: String
}
