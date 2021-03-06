package com.tzion.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tzion.navigation.Actions
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_open_movies.setOnClickListener {
            startActivity(Actions.getOpenMoviesIntent(this))
        }

        btn_open_about.setOnClickListener {
            startActivity(Actions.getAboutIntent(this))
        }
    }

}
