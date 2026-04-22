package com.ujjwal.speedmath.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ujjwal.speedmath.data.repository.MathRepository
import com.ujjwal.speedmath.utils.StreakManager
import kotlinx.coroutines.launch

data class HomeStats(
    val totalSessions: Int,
    val currentStreak: Int,
    val bestStreak: Int,
    val overallAccuracy: Float
)

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = MathRepository(app)
    private val streakManager = StreakManager(app)

    val stats = MutableLiveData<HomeStats>()

    fun loadStats() {
        viewModelScope.launch {
            val total = repo.getTotalSessions()
            val acc   = repo.getOverallAccuracy()
            stats.value = HomeStats(
                totalSessions   = total,
                currentStreak   = streakManager.getCurrentStreak(),
                bestStreak      = streakManager.getBestStreak(),
                overallAccuracy = acc
            )
        }
    }
}
