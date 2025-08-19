package com.only.engine.web.wrapper

import com.fasterxml.jackson.annotation.JsonIgnore
import com.only.engine.enums.Desc
import com.only.engine.enums.Name

abstract class ResultMixIn : Desc, Name{

    @get:JsonIgnore
    abstract override val desc: String

    @get:JsonIgnore
    abstract override val name: String
}
