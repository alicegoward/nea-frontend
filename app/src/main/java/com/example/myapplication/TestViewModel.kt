package com.example.myapplication

import androidx.lifecycle.ViewModel
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

class TestViewModel : ViewModel() {
    val sampleAPI: SampleAPI = RetrofitHelper.getInstance().create(SampleAPI::class.java)
    var data by mutableStateOf("help")
    init {
        Log.d("aaaa", "view model")
    }

    fun getData() {
        CoroutineScope(Dispatchers.IO).launch {
            data = sampleAPI.getData(Random.nextInt(200).toString()).body().toString()
            Log.d("aa", data)
        }
    }
}