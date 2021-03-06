package com.melon

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.melon.databinding.FragmentPlayerBinding
import com.melon.service.MusicDto
import com.melon.service.MusicService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class PlayerFragment : Fragment(R.layout.fragment_player) {

    private var model: PlayerModel = PlayerModel()
    private var binding: FragmentPlayerBinding? = null

    // private var isWatchingPlayListView = true : μμΉμ΄λ(PlayerModel)
    private var player: SimpleExoPlayer? = null
    private lateinit var playListAdapter: PlayListAdapter

    private val updateSeekRunnable = Runnable {
        updateSeek()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentPlayerBinding = FragmentPlayerBinding.bind(view)
        binding = fragmentPlayerBinding

        initPlayView(fragmentPlayerBinding)
        initPlayListButton(fragmentPlayerBinding)
        initPlayControlButtons(fragmentPlayerBinding)
        initSeekBar(fragmentPlayerBinding)
        initRecyclerView(fragmentPlayerBinding)

        getVideoListFromServer()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initSeekBar(fragmentPlayerBinding: FragmentPlayerBinding) {
        fragmentPlayerBinding.playerSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                player?.seekTo((seekBar.progress * 1000).toLong())
            }
        })

        fragmentPlayerBinding.playListSeekBar.setOnTouchListener { v, event ->
            false
        }
    }

//     seekbar control
    private fun initPlayControlButtons(fragmentPlayerBinding: FragmentPlayerBinding) {
        fragmentPlayerBinding.playControlImageView.setOnClickListener {
            val player = this.player ?: return@setOnClickListener

            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        }

        fragmentPlayerBinding.skipNextImageView.setOnClickListener {
            val nextMusic = model.nextMusic() ?: return@setOnClickListener
            playMusic(nextMusic)
        }

        fragmentPlayerBinding.skipPrevImageView.setOnClickListener {
            val prevMusic = model.prevMusic() ?: return@setOnClickListener
            playMusic(prevMusic)
        }
    }

//    νλ¨ playview μ»¨νΈλ‘€
    private fun initPlayView(fragmentPlayerBinding: FragmentPlayerBinding) {
        context?.let { // player κ° μ§κΈ null μ΄κΈ° λλ¬Έμ μ΄κΈ°ννκ³  λ£μ΄μ€
            player = SimpleExoPlayer.Builder(it).build()
        }
        fragmentPlayerBinding.playerView.player = player

        binding?.let { binding ->
            player?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)

                    if (isPlaying) {
                        binding.playControlImageView.setImageResource(R.drawable.ic_baseline_pause_48)
                    } else {
                        binding.playControlImageView.setImageResource(R.drawable.ic_baseline_play_arrow_48)
                    }
                }

                override fun onPlaybackStateChanged(state: Int) {
                    super.onPlaybackStateChanged(state)

                    updateSeek()
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    super.onMediaItemTransition(mediaItem, reason)

                    val newIndex = mediaItem?.mediaId ?: return
                    model.currentPosition = newIndex.toInt()
                    updatePlayerView(model.currentMusicModel())

                    playListAdapter.submitList(model.getAdapterModels())
                    // DiffUtil μ ν΅ν UI μλ°μ΄νΈλΉμ© : λ?μ.
                    // data class - Copy λ₯Ό ν΅ν΄ isPlaying κ°λ§ λ°κΎΈμ΄ μ£ΌμκΈ° λλ¬Έμ
                    // μ μ²΄List λ€μ κ·Έλ¦¬λκ² μλλΌ isPlaying μ΄ true > false νΉμ false > true λ λΆλΆλ§ λ¦¬νλμ¬ ν΄μ€
                }
            })
        }
    }

    private fun updateSeek() {

        val player = this.player ?: return // nullμ²λ¦¬
        val duration = if (player.duration >= 0) player.duration else 0
        val position = player.currentPosition

        updateSeekUi(duration, position)

        val state = player.playbackState

        view?.removeCallbacks(updateSeekRunnable)
        if(state != Player.STATE_IDLE && state != Player.STATE_ENDED) {
            view?.postDelayed(updateSeekRunnable, 1000)
        }
    }

//    μμ λΆ μ΄ UI
    private fun updateSeekUi(duration: Long, position: Long) {

        binding?.let{ binding ->
            binding.playListSeekBar.max = (duration / 1000).toInt()
            binding.playListSeekBar.progress = (position / 1000).toInt()

            binding.playerSeekBar.max = (duration / 1000).toInt()
            binding.playerSeekBar.progress = (position / 1000).toInt()

            binding.playTimeTextView.text = String.format("%02d:%02d",
                TimeUnit.MINUTES.convert(position, TimeUnit.MILLISECONDS),
                (position / 1000) % 60)
            binding.totalTimeTextView.text = String.format("%02d:%02d",
                TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS),
                (duration / 1000) % 60)
        }
    }

    private fun updatePlayerView(currentMusicModel: MusicModel?) {
        currentMusicModel ?: return

        binding?.let { binding ->
            binding.trackTextView.text = currentMusicModel.track
            binding.artistTextView.text = currentMusicModel.artist
            Glide.with(binding.coverImageView.context)
                .load(currentMusicModel.coverUrl)
                .into(binding.coverImageView)

        }
    }
// > μμ μ ν,ν΄λ¦­
    private fun initRecyclerView(fragmentPlayerBinding: FragmentPlayerBinding) {
        playListAdapter = PlayListAdapter {
            playMusic(it)
        }

        fragmentPlayerBinding.playListRecyclerView.apply {
            adapter = playListAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

//    >μ¬μλͺ©λ‘
    private fun initPlayListButton(fragmentPlayerBinding: FragmentPlayerBinding) {
        fragmentPlayerBinding.playlistImageView.setOnClickListener {
            if (model.currentPosition == -1) return@setOnClickListener
            fragmentPlayerBinding.playerViewGroup.isVisible = model.isWatchingPlayListView
            fragmentPlayerBinding.playerListViewGroup.isVisible = model.isWatchingPlayListView.not()

            model.isWatchingPlayListView = !model.isWatchingPlayListView

            binding?.let { binding ->
                if (model.isWatchingPlayListView) {
                    apply {
                        binding.playControlImageView.setColorFilter(
                            Color.WHITE,
                            PorterDuff.Mode.SRC_IN
                        )
                        binding.playlistImageView.setColorFilter(
                            Color.WHITE,
                            PorterDuff.Mode.SRC_IN
                        )
                        binding.skipNextImageView.setColorFilter(
                            Color.WHITE,
                            PorterDuff.Mode.SRC_IN
                        )
                        binding.skipPrevImageView.setColorFilter(
                            Color.WHITE,
                            PorterDuff.Mode.SRC_IN
                        )
                    }
                } else {
                    apply {
                        binding.playControlImageView.setColorFilter(
                            Color.BLACK,
                            PorterDuff.Mode.SRC_IN
                        )
                        binding.playlistImageView.setColorFilter(
                            Color.BLACK,
                            PorterDuff.Mode.SRC_IN
                        )
                        binding.skipNextImageView.setColorFilter(
                            Color.BLACK,
                            PorterDuff.Mode.SRC_IN
                        )
                        binding.skipPrevImageView.setColorFilter(
                            Color.BLACK,
                            PorterDuff.Mode.SRC_IN
                        )
                    }
                }
            }
        }
    }

    private fun getVideoListFromServer() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://run.mocky.io")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(MusicService::class.java)
            .also {
                it.listMusics()
                    .enqueue(object : Callback<MusicDto> {
                        override fun onResponse(
                            call: Call<MusicDto>,
                            response: Response<MusicDto>
                        ) {
                            response.body()?.let { musicDto ->
//                                val modelList = it.musics.mapIndexed { index, musicEntity ->
//                                    musicEntity.mapper(index.toLong()) // νμ₯ν΄μ mapper μ μΈν΄μ€¬κΈ° λλ¬Έμ κ°λ₯
//                                }
//                                   λ§μ°¬κ°μ§λ‘ MusicModelMapperλ‘ μ΄μ 

                                // λ°μ΄ν°λ₯Ό μλ²μμ λΆλ¬μ€λ λΆλΆμ model ν΄λμ€λ₯Ό μ΄κΈ°νν€μ£Όλ©°,
                                // λͺ¨λΈμ modelList λ₯Ό λ°λ‘ λ£μμ μκΈ° λλ¬Έμ
                                // MusicModelMapper μ μ μΈν΄μ€(λ§€ν)
                                model = musicDto.mapper()

                                setMusicList(model.getAdapterModels())
                                playListAdapter.submitList(model.getAdapterModels())
                            }
                        }

                        override fun onFailure(call: Call<MusicDto>, t: Throwable) {

                        }

                    })
            }
    }

    private fun setMusicList(modelList: List<MusicModel>) {
        context?.let {
            player?.addMediaItems(modelList.map { musicModel ->
                MediaItem.Builder()
                    .setMediaId(musicModel.id.toString())
                    .setUri(musicModel.streamUrl)
                    .build()
            })

            player?.prepare()
        }
    }

    private fun playMusic(musicModel: MusicModel) {
        model.updateCurrentPosition(musicModel)
        player?.seekTo(model.currentPosition, 0)
        player?.play()
    }

    override fun onStop() {
        super.onStop()

        player?.pause()
        view?.removeCallbacks(updateSeekRunnable)
    }
//μλͺμ£ΌκΈ° μμ£Όλ‘ upgrade
    override fun onDestroy() {
        super.onDestroy()

        binding = null
        player?.release()
        view?.removeCallbacks(updateSeekRunnable)
    }

    companion object { // newInstance λ‘ μΈμλ₯Ό λκ²¨μ€ λ apply λ₯Ό ν΅ν΄ arguments λ₯Ό μ½κ² μΆκ°ν΄μ€ μ μλ€.
        fun newInstance(): PlayerFragment {
            return PlayerFragment()
        }
    }

}