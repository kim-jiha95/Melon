package com.melon

data class MusicModel(
    val id: Long, //리사이클러뷰 어뎁터에서 DiffUtil 을통해 같은 값인지 확인할 목적 id 값은 playlist 의 위치로 줄것임
    val track: String,
    val streamUrl: String,
    val artist: String,
    val coverUrl: String,
    val isPlaying: Boolean = false
)