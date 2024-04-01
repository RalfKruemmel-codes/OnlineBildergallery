package com.example.myapplication

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.io.IOException
import kotlin.math.abs
import android.app.Activity
import android.widget.BaseAdapter
import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestOptions
import android.os.Handler;
import android.widget.TextView
import android.os.Looper


class DownloadCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == intent.action) {
            Toast.makeText(context, "Download abgeschlossen", Toast.LENGTH_SHORT).show()
        }
    }
}

class MainActivity : AppCompatActivity() {


    private var currentImageNumber = 1
    private val IMAGE_REQUEST_CODE = 100

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val imageView = findViewById<ImageView>(R.id.imageView)
        val textViewInfo: TextView = findViewById(R.id.textViewInfo)
        Handler(Looper.getMainLooper()).postDelayed({
            textViewInfo.visibility = View.GONE
        }, 5000)

        Glide.with(this)
            .load("https://lichtbringer.online/logo1.gif")
            .transition(withCrossFade())
            .into(imageView)
        val button = findViewById<Button>(R.id.button)
        val button2 = findViewById<Button>(R.id.button2)
        val button3 = findViewById<Button>(R.id.button3)
        val buttonSetWallpaper = findViewById<Button>(R.id.buttonSetWallpaper)
        val buttonGallery = findViewById<Button>(R.id.buttonGallery)


        val startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    currentImageNumber = data?.getIntExtra("selectedImage", 1) ?: 1
                    loadImage(findViewById(R.id.imageView))
                }
            }


        imageView.setOnTouchListener(object : View.OnTouchListener {
            private val SWIPE_THRESHOLD = 100
            private var downX: Float = 0.toFloat()
            private var downY: Float = 0.toFloat()

            val gestureDetector = GestureDetectorCompat(
                this@MainActivity,
                object : GestureDetector.SimpleOnGestureListener() {
                    override fun onDoubleTap(e: MotionEvent): Boolean {
                        downloadImage()
                        return super.onDoubleTap(e)
                    }
                })

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                gestureDetector.onTouchEvent(event)

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = event.x
                        downY = event.y
                    }

                    MotionEvent.ACTION_UP -> {
                        val deltaX = event.x - downX
                        val deltaY = event.y - downY

                        if (abs(deltaX) > SWIPE_THRESHOLD && abs(deltaX) > abs(deltaY)) {
                            if (deltaX > 0) {
                                onSwipeRight()
                            } else {
                                onSwipeLeft()
                            }
                        }
                    }
                }
                return true
            }

            fun onSwipeRight() {
                if (currentImageNumber > 1) {
                    currentImageNumber--
                }
                loadImage(imageView)
            }

            fun onSwipeLeft() {
                if (currentImageNumber < 1500) {
                    currentImageNumber++
                }
                loadImage(imageView)
            }
        })

        button.setOnClickListener {
            if (currentImageNumber > 1) {
                currentImageNumber--
            }
            loadImage(imageView)
        }

        button2.setOnClickListener {
            if (currentImageNumber < 1500) {
                currentImageNumber++
            }
            loadImage(imageView)
        }

        button3.setOnClickListener {
            downloadImage()
        }

        buttonSetWallpaper.setOnClickListener {
            setAsWallpaper()
        }

        buttonGallery.setOnClickListener {
            val intent = Intent(this, GalleryActivity::class.java)
            startForResult.launch(intent)
        }
    }


    private fun loadImage(imageView: ImageView) {
        Glide.with(this)
            .load("https://lichtbringer.online/images/bild$currentImageNumber.jpg")
            .into(imageView)
    }

    private fun downloadImage() {
        val url = "https://lichtbringer.online/images/bild$currentImageNumber.jpg"
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Bild$currentImageNumber.jpg")
            .setDescription("Downloading")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Bild$currentImageNumber.jpg")
            .setAllowedOverMetered(true)
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadID = downloadManager.enqueue(request)

        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) == downloadID) {
                    Toast.makeText(this@MainActivity, "Download abgeschlossen", Toast.LENGTH_SHORT).show()
                }
            }
        }

        registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    @SuppressLint("MissingPermission")
    private fun setAsWallpaper() {
        val wallpaperManager = WallpaperManager.getInstance(this)
        Glide.with(this)
            .asBitmap()
            .load("https://lichtbringer.online/images/bild$currentImageNumber.jpg")
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    try {
                        wallpaperManager.setBitmap(resource)
                        Toast.makeText(this@MainActivity, "Hintergrundbild erfolgreich gesetzt", Toast.LENGTH_SHORT).show()
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this@MainActivity, "Fehler beim Setzen des Hintergrundbildes", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Ignorieren Sie dies, wenn Sie kein Platzhalterbild anzeigen möchten
                }
            })
    }
}

class ImageAdapter(private val context: Context, private val images: List<Int>) : BaseAdapter() {

    override fun getCount(): Int {
        return images.size
    }

    override fun getItem(position: Int): Any {
        return images[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val imageView = if (convertView == null) {
            LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false) as ImageView
        } else {
            convertView as ImageView
        }

        Glide.with(context)
            .load("https://lichtbringer.online/images/bild${getItem(position)}.jpg")
            .override(350, 350) // Fügen Sie diese Zeile hinzu
            .into(imageView)

        return imageView
    }
}


