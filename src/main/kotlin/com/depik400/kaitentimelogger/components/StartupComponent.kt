package com.depik400.kaitentimelogger.components

//package com.depik400.kaitentimelogger.components

import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.diagnostic.logger

class StartupComponent : ApplicationComponent {

    private val LOG = logger<StartupComponent>()

    override fun initComponent() {
        LOG.info("Kaiten Time Logger plugin initialized")
    }

    override fun disposeComponent() {
        LOG.info("Kaiten Time Logger plugin disposed")
    }
}