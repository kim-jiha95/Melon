package com.melon

import com.melon.service.MusicDto
import com.melon.service.MusicEntity

// MusicEntity 에서 MusicModel 로 바꿔주기 위함

fun MusicEntity.mapper(id: Long): MusicModel =
    MusicModel(
        id = id,
        track = this.track,  //this 빼도 무방
        streamUrl = streamUrl,
        artist = artist,
        coverUrl = coverUrl
    )
// mapper 를 통해서 초기화
fun MusicDto.mapper(): PlayerModel =
    PlayerModel(
        playMusicList = musics.mapIndexed { index, musicEntity ->
            musicEntity.mapper(index.toLong())
        }
)