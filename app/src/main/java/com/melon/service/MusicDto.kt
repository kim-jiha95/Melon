package com.melon.service

data class MusicDto(
    // 이전에는 서버에서 내려오는 객체와 뷰에서 사용하는 객체를 동일시해서 같은 모델을 사용했지만 이제부턴
    // 서버에서 받아오는 Entity 와 뷰에서 사용할 Model 로 분리 하고 중간에서 mapping 을 해줄것
    val musics: List<MusicEntity>
)