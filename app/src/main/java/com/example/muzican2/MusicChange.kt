package com.example.muzican2

import androidx.lifecycle.MutableLiveData

class MusicChange {
    companion object {
        var musicChangeListen = MutableLiveData(0)
    }
}