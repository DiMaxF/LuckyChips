package com.oleg2013.casino

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.TransitionDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import androidx.room.Room.databaseBuilder
import com.oleg2013.casino.Data.OptionsFields
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    private lateinit var menuScreen: FrameLayout
    private lateinit var gameScreen: FrameLayout
    private lateinit var optionsScreen: FrameLayout
    private lateinit var infoScreen: FrameLayout

    private lateinit var iconInfo: ImageView
    private lateinit var iconMenu: ImageView
    private lateinit var iconOptions: ImageView

    private lateinit var buttonInfo: Button
    private lateinit var buttonMenu: Button
    private lateinit var buttonOptions: Button

    private lateinit var navigationButtons: List<Button>

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
    private lateinit var click: Animation

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


    private val appDatabase: Data.AppDatabase by lazy {
        databaseBuilder(applicationContext, Data.AppDatabase::class.java, "options.db")
            .allowMainThreadQueries().build()
    }

    //region Override Functions

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        fromLeft = AnimationUtils.loadAnimation(this, R.anim.from_left)
        fromRight = AnimationUtils.loadAnimation(this, R.anim.from_right)
        click = AnimationUtils.loadAnimation(this, R.anim.click)

        menuScreen = findViewById(R.id.menu)
        gameScreen = findViewById(R.id.game)
        optionsScreen = findViewById(R.id.options)
        infoScreen = findViewById(R.id.info)

        iconInfo = findViewById(R.id.navigation_info)
        iconOptions = findViewById(R.id.navigation_settings)
        iconMenu = findViewById(R.id.navigation_menu)

        buttonInfo = findViewById(R.id.button_info)
        buttonOptions = findViewById(R.id.button_options)
        buttonMenu = findViewById(R.id.button_menu)

        screens = listOf(menuScreen, gameScreen, optionsScreen, infoScreen)
        navigationButtons = listOf(buttonMenu, buttonInfo, buttonOptions)

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
        updateNavigation(buttonMenu)
        setToolbarTitle("Hello!")

        if(!appDatabase.getStatisticDao().isExist(Variables.soundName)){
            val soundsField = OptionsFields(Variables.soundName, true)
            appDatabase.getStatisticDao().insertAll(soundsField)
        }
        if(!appDatabase.getStatisticDao().isExist(Variables.musicName)){
            val musicField = OptionsFields(Variables.musicName, true)
            appDatabase.getStatisticDao().insertAll(musicField)
        }
        if(!appDatabase.getStatisticDao().isExist(Variables.vibrationName)){
            val vibrationField = OptionsFields(Variables.vibrationName, true)
            appDatabase.getStatisticDao().insertAll(vibrationField)
        }
        setTogglesValues()
    }

    //endregion

    //region Navigation

    fun onButtonMenu(view: View) {
        switchScreen(menuScreen)
        toolbarShow(true)
        setToolbarTitle("Menu")
        iconMenu.startAnimation(click)
        updateNavigation(view)
    }

    fun onButtonOptions(view: View) {
        switchScreen(optionsScreen)
        toolbarShow(true)
        setToolbarTitle("Options")
        iconOptions.startAnimation(click)
        updateNavigation(view)
    }

    fun onButtonInfo(view: View) {
        switchScreen(infoScreen)
        toolbarShow(true)
        setToolbarTitle("Info")
        iconInfo.startAnimation(click)
        updateNavigation(view)
    }

    private fun updateNavigation(view: View){
        for(button in navigationButtons){
            var color = getColor(if(view.id == button.id) R.color.focused else R.color.clear)
            colorTransition(button, color, 200)
        }
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

    private fun setTogglesValues(){
        val musicValue = appDatabase.getStatisticDao().getFieldByName(Variables.musicName)!!.value
        val soundsValue = appDatabase.getStatisticDao().getFieldByName(Variables.soundName)!!.value
        val vibrationValue = appDatabase.getStatisticDao().getFieldByName(Variables.vibrationName)!!.value
        toggleMusic.isChecked = musicValue
        toggleSounds.isChecked = soundsValue
        toggleVibration.isChecked = vibrationValue
        updateTogglesValue()
    }

    private fun updateTogglesValue(){
        Variables.sounds = toggleSounds.isChecked
        Variables.vibration = toggleVibration.isChecked
        Variables.music = toggleMusic.isChecked
        playAudio(R.raw.switch_toggle)
    }

    private fun onToggleSounds(value: Boolean, view: ToggleButton){
        updateTogglesValue()
        val colorTarget = if(value) getColor(R.color.purple_500) else getColor(R.color.teal_700)
        colorTransition(view, colorTarget)
        appDatabase.getStatisticDao().updateFieldValue(Variables.soundName, value)
    }

    private fun onToggleMusic(value: Boolean, view: ToggleButton){
        updateTogglesValue()
        if(!value) offAudio()
        val colorTarget = if(value) getColor(R.color.purple_500) else getColor(R.color.teal_700)
        colorTransition(view, colorTarget)
        appDatabase.getStatisticDao().updateFieldValue(Variables.musicName, value)
    }

    private fun onToggleVibration(value: Boolean, view: ToggleButton){
        updateTogglesValue()
        val colorTarget = if(value) getColor(R.color.purple_500) else getColor(R.color.teal_700)
        colorTransition(view, colorTarget)
        appDatabase.getStatisticDao().updateFieldValue(Variables.vibrationName, value)
    }

    fun onButtonRate(view: View){
        try{
            val rateIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appContext.packageName))
            startActivity(rateIntent)
        }catch (e: Exception){
            Log.e("LuckyChips", "Don't find the app in store")
            Toast.makeText(appContext, "Don't find the app in store", Toast.LENGTH_LONG).show()
        }

    }

    fun onButtonShare(view: View){
        val intent= Intent()
        intent.action=Intent.ACTION_SEND
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_description))
        intent.type="text/plain"
        startActivity(Intent.createChooser(intent,"Share To:"))
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
    fun colorTransition(view: View, targetColor: Int, duration: Int = 500){
        val startColor = view.solidColor
        val colors = arrayOf(ColorDrawable(startColor), ColorDrawable(targetColor))
        val trans = TransitionDrawable(colors)
        view.setBackgroundDrawable(trans)
        trans.startTransition(duration)
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