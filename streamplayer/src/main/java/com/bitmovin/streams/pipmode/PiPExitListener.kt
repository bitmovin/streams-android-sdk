package com.bitmovin.streams.pipmode

internal interface PiPExitListener {
    fun onPiPExit()

    fun isInPiPMode(): Boolean
}
