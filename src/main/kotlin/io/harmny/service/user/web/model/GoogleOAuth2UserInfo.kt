package io.harmny.service.user.web.model

class GoogleOAuth2UserInfo(attributes: Map<String, Any>) : OAuth2UserInfo(attributes) {

    override fun getId() = attributes["sub"] as String
    override fun getName() = attributes["name"] as String
    override fun getEmail() = attributes["email"] as String
    override fun getImageUrl() = attributes["picture"] as String?
}
