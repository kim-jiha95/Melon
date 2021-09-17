package com.melon

data class PlayerModel (
    private val playMusicList: List<MusicModel> = emptyList(), //초기화 안되있어서 playerFragment 에서 바로 사용이 불가하니 이곳에서 초기값 선언
    var currentPosition: Int = -1, // 초기화 안된값을 의미 0이면 첫번째 아이템을 말하는거라서 -1로
    var isWatchingPlayListView: Boolean = true
) {

    fun getAdapterModels(): List<MusicModel> {
        return playMusicList.mapIndexed { index, musicModel ->
            val newItem = musicModel.copy(
                isPlaying = index == currentPosition
            )
            newItem
        }
    }

    fun updateCurrentPosition(musicModel: MusicModel) {
        currentPosition = playMusicList.indexOf(musicModel)
    }

    fun nextMusic(): MusicModel? {
        if(playMusicList.isEmpty()) return null

        //마지막곡에서 다음곡 갈 때 포지션 변화
        currentPosition = if((currentPosition + 1) == playMusicList.size) 0 else currentPosition + 1
        return playMusicList[currentPosition]
    }

    fun prevMusic(): MusicModel? {
        if(playMusicList.isEmpty()) return null

        //첫곡에서 이전곡 갈 때 포지션 변화
        currentPosition = if((currentPosition -1 ) < 0) playMusicList.lastIndex else currentPosition - 1
        return playMusicList[currentPosition]
    }

    fun currentMusicModel(): MusicModel? {
        if(playMusicList.isEmpty()) return null

        return playMusicList[currentPosition]
    }
}

// data class 의 강력한 기능 copy : 우리가 수정하려는 값만 수정하고 클래스 자체를 새로 만들어줌
// 만약 리사이클러뷰의 어뎁터안에있는 값을 수정하게 되면
// 새로운 값과 원래있던 값 모두 수정이 되는거기 때문에 수정되어도 수정된값을 인식하지 못한다.
// WHY ?? areItemSames 라는 DiffUtil 에서 값을 볼 때 oldItem 과 newItem 의 값이 같은지 비교해주는데
// 이미 둘다 초기화가 되어서 같은아이템으로 인식하고 뷰를 갱신하지 않음
// 때문에 copy 를 이용해서 새로운값만 갱신된 상태로.