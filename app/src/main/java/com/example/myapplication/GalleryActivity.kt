package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.GridView
import androidx.appcompat.app.AppCompatActivity

class GalleryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        val images = (1..1500).toList<Int>()
        val gridView = findViewById<GridView>(R.id.gridView)
        gridView.adapter = ImageAdapter(this, images)

        gridView.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent()
            intent.putExtra("selectedImage", images[position])
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}