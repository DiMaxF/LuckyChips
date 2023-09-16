package com.oleg2013.casino

import android.R.color
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.TransitionDrawable
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    private lateinit var menuScreen: FrameLayout
    private lateinit var gameScreen: FrameLayout
    private lateinit var optionsScreen: FrameLayout
    private lateinit var infoScreen: FrameLayout

    private lateinit var screens: List<FrameLayout>

    private lateinit var toggleSounds: ToggleButton
    private lateinit var toggleMusic: ToggleButton
    private lateinit var toggleVibration: ToggleButton

    private lateinit var chip1: View
    private lateinit var chip2: View

    private lateinit var gameBoard: FrameLayout
    private lateinit var win: FrameLayout

    private lateinit var winPanel: FrameLayout
    private lateinit var losePanel: FrameLayout

    private lateinit var navigation: FrameLayout

    private lateinit var fadeIn: Animation
    private lateinit var fadeOut: Animation
    private lateinit var fromLeft: Animation
    private lateinit var fromRight: Animation

    private val dragListener = View.OnDragListener{ view, event ->
        when(event.action) {
            DragEvent.ACTION_DRAG_STARTED ->{
                event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN )
                vibrate(300)
                true
            }
            DragEvent.ACTION_DRAG_ENTERED ->{
                view.invalidate()
                true
            }
            DragEvent.ACTION_DRAG_LOCATION -> true
            DragEvent.ACTION_DRAG_EXITED ->{
                view.invalidate()
                true
            }
            DragEvent.ACTION_DROP ->{
                val item = event.clipData.getItemAt(0)
                val dragData = item.text
                val v = event.localState as View
                val owner =  v.parent as ViewGroup
                owner.removeView(v)
                val destination = view as FrameLayout
                destination.addView(v)
                v.visibility = View.VISIBLE
                resultGame()
                true
            }
            DragEvent.ACTION_DRAG_ENDED ->{
                view.invalidate()
                true
            }else -> false
        }
    }

    companion object {
        lateinit var appContext: Context
    }

    //region Override Functions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        fromLeft = AnimationUtils.loadAnimation(this, R.anim.from_left)
        fromRight = AnimationUtils.loadAnimation(this, R.anim.from_right)

        menuScreen = findViewById(R.id.menu)
        gameScreen = findViewById(R.id.game)
        optionsScreen = findViewById(R.id.options)
        infoScreen = findViewById(R.id.info)

        screens = listOf(menuScreen, gameScreen, optionsScreen, infoScreen)

        toggleSounds = findViewById(R.id.toggle_sounds)
        toggleMusic = findViewById(R.id.toggle_music)
        toggleVibration = findViewById(R.id.toggle_vibration)

        winPanel = findViewById(R.id.win_panel)
        losePanel = findViewById(R.id.lose_panel)

        navigation = findViewById(R.id.navigation)

        gameBoard = findViewById(R.id.game_board)
        win = findViewById(R.id.win)

        chip1 = findViewById(R.id.chip1)
        chip2 = findViewById(R.id.chip2)

        toggleSounds.setOnCheckedChangeListener { _, isChecked  ->
            onToggleSounds(isChecked, toggleSounds);
        }
        toggleMusic.setOnCheckedChangeListener { _, isChecked  ->
            onToggleMusic(isChecked, toggleMusic);
        }
        toggleVibration.setOnCheckedChangeListener { _, isChecked  ->
            onToggleVibration(isChecked, toggleVibration);
        }

        appContext = applicationContext
        switchScreen(menuScreen)

        setToolbarTitle("Hello!")
    }

    //endregion

    //region Navigation

    fun onButtonMenu(view: View) {
        switchScreen(menuScreen)
        toolbarShow(true)
        setToolbarTitle("Menu")
    }

    fun onButtonOptions(view: View) {
        switchScreen(optionsScreen)
        toolbarShow(true)
        setToolbarTitle("Options")
    }

    fun onButtonInfo(view: View) {
        switchScreen(infoScreen)
        toolbarShow(true)
        setToolbarTitle("Info")
    }

    private fun switchScreen(show: FrameLayout){

        for(screen in screens){
            visibility(screen.id == show.id, screen)
            screen.clearAnimation()
            if(screen.id == show.id) screen.startAnimation(fadeIn)
        }

        playAudio(R.raw.click)
    }

    private fun visibility(value: Boolean, view: FrameLayout){
        view.visibility = if(value){ View.VISIBLE} else{View.INVISIBLE}
    }

    //endregion

    //region Settings

    private fun onToggleSounds(value: Boolean, view: ToggleButton){
        Variables.sounds = value
        playAudio(R.raw.switch_toggle)
        val colorTarget = if(value) getColor(R.color.purple_500) else getColor(R.color.teal_700)
        colorTransition(view, colorTarget)
    }

    private fun onToggleMusic(value: Boolean, view: ToggleButton){
        Variables.music = value
        if(!value) offAudio()
        playAudio(R.raw.switch_toggle)
        val colorTarget = if(value) getColor(R.color.purple_500) else getColor(R.color.teal_700)
        colorTransition(view, colorTarget)
    }

    private fun onToggleVibration(value: Boolean, view: ToggleButton){
        Variables.vibration = value
        playAudio(R.raw.switch_toggle)
        val colorTarget = if(value) getColor(R.color.purple_500) else getColor(R.color.teal_700)
        colorTransition(view, colorTarget)
    }

    //endregion

    //region Audio

    enum class AudioType{
        Sounds,
        Music
    }

    var loop: MediaPlayer = MediaPlayer()
    var audio: MediaPlayer = MediaPlayer()

    private fun playAudio(id: Int, type: AudioType = AudioType.Sounds) {

        if (type == AudioType.Sounds && Variables.sounds) {
            audio = MediaPlayer.create(this, id)
            audio!!.isLooping = false
            if (audio.isPlaying) {
                audio.stop();
                audio.prepare();
            }
            audio.start();
        }

        if(type == AudioType.Music && Variables.music){
            loop = MediaPlayer.create(this, id)
            loop.isLooping = false
            loop.start()
        }
    }

    private fun offAudio(){
        loop!!.pause()
    }

    private fun vibrate(duration: Long){
        if(!Variables.vibration) return

        val vibrator = appContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(duration)
        }
    }

    //endregion

    //region Game

    fun onButtonGame(view: View) {
        switchScreen(gameScreen)
        toolbarShow(false)
        startNewGame()
    }

    private fun startNewGame(){
        visibility(false, winPanel)
        visibility(false, losePanel)

        navigation.startAnimation(fadeOut)
        visibility(false, navigation)

        win.setOnDragListener(dragListener)
        gameBoard.setOnDragListener(dragListener)

        playAudio(R.raw.loop_menu, AudioType.Music)

        chip1.setOnLongClickListener {
            val clipText = "Chip 1"
            val item = ClipData.Item(clipText)
            val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
            val data = ClipData(clipText, mimeTypes, item)
            val dragShadowBuilder = View.DragShadowBuilder(it)
            it.startDragAndDrop(data, dragShadowBuilder, it, 0)
            it.visibility = View.INVISIBLE
            true
        }
        chip2.setOnLongClickListener {
            val clipText = "Chip 2"
            val item = ClipData.Item(clipText)
            val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
            val data = ClipData(clipText, mimeTypes, item)
            val dragShadowBuilder = View.DragShadowBuilder(it)
            it.startDragAndDrop(data, dragShadowBuilder, it, 0)
            it.visibility = View.INVISIBLE
            true
        }
    }

    private fun resultGame(){
        chip1.setOnLongClickListener(null)
        chip2.setOnLongClickListener(null)
        win.setOnDragListener(null)
        gameBoard.setOnDragListener(null)
        playAudio(R.raw.chip_set)
        vibrate(300)
        offAudio()
        Handler().postDelayed({
            vibrate(1000)
            if(Random.nextInt(0, 2) == 0) win()
            else lose()
        }, 1000)
    }

    private fun win(){
        visibility(true, winPanel)
        winPanel.startAnimation(fadeIn)
        winPanel.startAnimation(fromLeft)
        playAudio(R.raw.win)
    }

    private fun lose(){
        visibility(true, losePanel)
        losePanel.startAnimation(fadeIn)
        losePanel.startAnimation(fromRight)
        playAudio(R.raw.lose)
    }

    fun endGame(view: View){
        finish();
        overridePendingTransition( 0, 0);
        startActivity(getIntent());
        overridePendingTransition( 0, 0);
    }

    //endregion

    //region Utils

    @SuppressLint("ObjectAnimatorBinding")
    fun colorTransition(view: View, targetColor: Int){
        val startColor = view.solidColor
        val colors = arrayOf(ColorDrawable(startColor), ColorDrawable(targetColor))
        val trans = TransitionDrawable(colors)
        view.setBackgroundDrawable(trans)
        trans.startTransition(500)
    }

    private fun setToolbarTitle(title: String){
        val actionbar: androidx.appcompat.app.ActionBar? = supportActionBar
        val textview = TextView(this@MainActivity)
        val layoutparams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )

        textview.layoutParams = layoutparams
        textview.text = title
        textview.setTextColor(Color.WHITE)
        textview.textSize = 22f
        textview.setTypeface(null, Typeface.BOLD);

        actionbar?.displayOptions = androidx.appcompat.app.ActionBar.DISPLAY_SHOW_CUSTOM
        actionbar?.customView = textview
    }

    private fun toolbarShow(value: Boolean){
        val actionbar: androidx.appcompat.app.ActionBar? = supportActionBar
        if(value) {
            actionbar?.show()
        } else{
            actionbar?.hide()
        }
    }

    //endregion
}