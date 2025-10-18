package io.rebble.cobble.speex_codec

enum class SpeexDecodeResult {
    Success,
    EndOfStream,
    CorruptStream;

    companion object {
        fun fromInt(value: Int) = when (value) {
            0 -> Success
            -1 -> EndOfStream
            -2 -> CorruptStream
            else -> throw IllegalArgumentException("Invalid value for SpeexDecodeResult")
        }
    }
}
