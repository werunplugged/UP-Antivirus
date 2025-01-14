package com.unplugged.up_antivirus.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unplugged.up_antivirus.base.Utils
import kotlinx.coroutines.launch
import com.unplugged.up_antivirus.domain.use_case.GetHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(private val getHistoryUseCase: GetHistoryUseCase) : ViewModel() {

    private val _historyState = MutableLiveData<HistoryState>()
    val historyState: LiveData<HistoryState> = _historyState

    init {
        viewModelScope.launch {
            val history = getHistoryUseCase()
            _historyState.value = HistoryState(history = history)
            Utils.printLog(HistoryViewModel::class.java, "history -> ${history}")
        }
    }

}