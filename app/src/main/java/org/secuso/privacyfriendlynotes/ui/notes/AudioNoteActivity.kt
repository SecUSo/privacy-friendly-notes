/*
 This file is part of the application Privacy Friendly Notes.
 Privacy Friendly Notes is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.
 Privacy Friendly Notes is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License
 along with Privacy Friendly Notes. If not, see <http://www.gnu.org/licenses/>.
 */
package org.secuso.privacyfriendlynotes.ui.notes

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import org.secuso.privacyfriendlynotes.R
import org.secuso.privacyfriendlynotes.room.DbContract
import org.secuso.privacyfriendlynotes.room.model.Note
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.OutputStream

/**
 * Activity that allows to add, edit and delete audio notes.
 */
class AudioNoteActivity : BaseNoteActivity(DbContract.NoteEntry.TYPE_AUDIO) {
    private val btnPlayPause: ImageButton by lazy { findViewById(R.id.btn_play_pause) }
    private val btnRecord: ImageButton by lazy { findViewById(R.id.btn_record) }
    private val tvRecordingTime: TextView by lazy { findViewById(R.id.recording_time) }
    private val seekBar: SeekBar by lazy { findViewById(R.id.seekbar) }

    private var mRecorder: MediaRecorder? = null
    private var mPlayer: MediaPlayer? = null
    private val mHandler = Handler()
    private var mFileName = "finde_die_datei.mp4"
    private lateinit var mFilePath: String
    private var recording = false
    private var playing = false
    private var isEmpty = true
    private var startTime = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_audio_note)

        findViewById<View>(R.id.btn_record).setOnClickListener(this)
        btnPlayPause.setOnClickListener(this)

        if (ContextCompat.checkSelfPermission(this@AudioNoteActivity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@AudioNoteActivity, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_CODE_AUDIO)
        }
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (mPlayer != null && fromUser) {
                    mPlayer!!.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        super.onCreate(savedInstanceState)
    }

    override fun onLoadActivity() {}
    override fun onNewNote() {
        mFileName = "/recording_" + System.currentTimeMillis() + ".aac"
        mFilePath = filesDir.path + "/audio_notes"
        File(mFilePath).mkdirs() //ensure that the file exists
        mFilePath = filesDir.path + "/audio_notes" + mFileName
        seekBar.isEnabled = false
        tvRecordingTime.visibility = View.VISIBLE
    }

    override fun onNoteLoadedFromDB(note: Note) {
        mFileName = note.content
        mFilePath = filesDir.path + "/audio_notes" + mFileName
        btnPlayPause.visibility = View.VISIBLE
        btnRecord.visibility = View.INVISIBLE
        tvRecordingTime.visibility = View.INVISIBLE
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) {
            if (recording) {
                stopRecording()
                finish()
            } else if (playing) {
                pausePlaying()
            }
        }
    }

    override fun shareNote(name: String): ActionResult<Intent, Int> {
        val audioFile = File(mFilePath)
        val contentUri = FileProvider.getUriForFile(
            applicationContext,
            "org.secuso.privacyfriendlynotes",
            audioFile
        )
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.type = "audio/*"
        sendIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
        sendIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        return ActionResult(true, sendIntent)
    }

    override fun determineToSave(title: String, category: Int): Pair<Boolean, Int> {
        val intent = intent
        return Pair(
            seekBar.isEnabled && -5 != intent.getIntExtra(
                EXTRA_CATEGORY, -5
            ),
            R.string.toast_emptyNote
        )
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_record -> if (!recording) {
                startRecording()
            } else {
                stopRecording()
            }

            R.id.btn_play_pause -> if (!playing) {
                startPlaying()
            } else {
                pausePlaying()
            }

            else -> {}
        }
    }

    private fun startRecording() {
        isEmpty = false
        recording = true
        mRecorder = MediaRecorder()
        mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
        mRecorder!!.setOutputFile(mFilePath)
        mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        try {
            mRecorder!!.prepare()
            val animation: Animation = AlphaAnimation(1.0F, 0.5F) // Change alpha from fully visible to invisible
            animation.duration = 500 // duration - half a second
            animation.interpolator = LinearInterpolator() // do not alter animation rate
            animation.repeatCount = Animation.INFINITE // Repeat animation infinitely
            animation.repeatMode =
                Animation.REVERSE // Reverse animation at the end so the button will fade back in
            btnRecord.startAnimation(animation)
            startTime = System.currentTimeMillis()
            runOnUiThread(object : Runnable {
                override fun run() {
                    if (mRecorder != null) {
                        val time = System.currentTimeMillis() - startTime
                        var seconds = time.toInt() / 1000
                        val minutes = seconds / 60
                        seconds %= 60
                        tvRecordingTime.text =
                            String.format("%02d", minutes) + ":" + String.format("%02d", seconds)
                        mHandler.postDelayed(this, 100)
                    }
                }
            })
            mRecorder!!.start()
        } catch (e: IOException) {
            recording = false
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        Log.d("LALALA", "Stopped recording")
        mRecorder!!.stop()
        mRecorder!!.release()
        btnRecord.clearAnimation()
        mRecorder = null
        recording = false
        recordingFinished()
    }

    private fun startPlaying() {
        playing = true
        if (mPlayer == null) {
            mPlayer = MediaPlayer()
            mPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
            try {
                mPlayer!!.setDataSource(mFilePath)
                mPlayer!!.prepare()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        mPlayer!!.setOnCompletionListener {
            playing = false
            togglePlayPauseButton()
            seekBar.progress = 0
            mPlayer!!.release()
            mPlayer = null
        }
        togglePlayPauseButton()
        seekBar.max = mPlayer!!.duration
        runOnUiThread(object : Runnable {
            override fun run() {
                if (mPlayer != null) {
                    seekBar.progress = mPlayer!!.currentPosition
                    mHandler.postDelayed(this, 100)
                }
            }
        })
        mPlayer!!.start()
    }

    private fun pausePlaying() {
        playing = false
        togglePlayPauseButton()
        try {
            mPlayer!!.pause()
        } catch (stopException: RuntimeException) {
        }
    }

    private fun recordingFinished() {
        shouldSave = true
        btnRecord.visibility = View.INVISIBLE
        btnPlayPause.visibility = View.VISIBLE
        seekBar.isEnabled = true
    }

    private fun togglePlayPauseButton() {
        if (playing) {
            btnPlayPause.setBackgroundResource(R.drawable.ic_pause_black_24dp)
        } else {
            btnPlayPause.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp)
        }
    }

    override fun updateNoteToSave(name: String, category: Int): ActionResult<Note, Int> {
        return ActionResult(true, Note(name, mFileName, DbContract.NoteEntry.TYPE_AUDIO, category))
    }

    override fun noteToSave(name: String, category: Int): ActionResult<Note, Int> {
        if (isEmpty) {
            return ActionResult(false, null, null)
        }
        return ActionResult(true, Note(name, mFileName, DbContract.NoteEntry.TYPE_AUDIO, category))
    }

    override fun getFileExtension() = ".aac"
    override fun getMimeType() = "audio/mp4a-latm"

    override fun onSaveExternalStorage(outputStream: OutputStream) {
        FileInputStream(File(mFilePath)).use {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.transferTo(outputStream)
            } else {
                val buffer = ByteArray(8192)
                var length: Int
                while (it.read(buffer).also { length = it } != -1) {
                    outputStream.write(buffer, 0, length)
                }
            }
        }
    }
}