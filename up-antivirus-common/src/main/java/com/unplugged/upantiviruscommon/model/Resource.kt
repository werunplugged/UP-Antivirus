package com.unplugged.upantiviruscommon.model

abstract class Resource<T> {
    companion object {
        fun <T> success(data: T): Data<T> {
            return Data(data)
        }

        fun <T> error(error: Throwable): Error<T> {
            return Error(error)
        }

        fun <T> loading(progress: Double = -1.0, max: Long = -1, type: ScannerType = ScannerType.NONE): Loading<T> {
            if (progress != -1.0 && max != -1L) {
                return Loading(Loading.Progress(progress, max, type))
            }
            return Loading()
        }

    }

    data class Error<V>(val error: Throwable) : Resource<V>()
    data class Data<V>(val data: V) : Resource<V>()
    data class Loading<V>(val progress: Progress? = null) : Resource<V>() {
        class Progress(val progress: Double, val max: Long, val type: ScannerType)
    }
}