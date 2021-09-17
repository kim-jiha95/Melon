package com.melon.service

import com.google.gson.annotations.SerializedName

data class MusicEntity(
    // 각 Entity 는 serializedName 으로 실제로 서버에서 내려오는 JSON 값 적어주기
    @SerializedName("track") val track: String,
    @SerializedName("streamUrl") val streamUrl: String,
    @SerializedName("artist") val artist: String,
    @SerializedName("coverUrl") val coverUrl: String
)