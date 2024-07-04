package com.bitmovin.testapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bitmovin.testapp.databinding.ActivityViewBinding

class FragmentTest : AppCompatActivity() {
    private lateinit var binding: ActivityViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
