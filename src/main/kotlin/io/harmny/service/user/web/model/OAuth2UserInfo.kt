package io.harmny.service.user.web.model

abstract class OAuth2UserInfo(
    protected val attributes: Map<String, Any>,
) {

    abstract fun getId(): String

    abstract fun getName(): String

    abstract fun getEmail(): String

    abstract fun getImageUrl(): String?
}
