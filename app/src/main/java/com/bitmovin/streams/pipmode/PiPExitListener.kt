package com.bitmovin.streams.pipmode

interface PiPExitListener {
    fun onPiPExit()
    fun isInPiPMode(): Boolean
}