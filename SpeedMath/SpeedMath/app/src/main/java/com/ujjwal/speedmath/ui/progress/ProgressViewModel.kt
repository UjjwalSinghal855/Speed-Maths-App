package com.ujjwal.speedmath.ui.progress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ujjwal.speedmath.data.entity.Session
import com.ujjwal.speedmath.data.repository.MathRepository
import com.ujjwal.speedmath.utils.StreakManager
import kotlinx.coroutines.launch

data class ProgressStats(
    val totalSessions: Int,
    val overallAccuracy: Float,
    val avgSpeedMs: Float,
    val bestSession: Session?,
    val currentStreak: Int,
    val bestStreak: Int,
    val recentSessions: List<Session>
)

class ProgressViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = MathRepository(app)
    private val streakManager = StreakManager(app)

    val allSessions: LiveData<List<Session>> = repo.getAllSessions()

    private val _stats = MutableLiveData<ProgressStats>()
    val stats: LiveData<ProgressStats> = _stats

    fun loadStats() {
        viewModelScope.launch {
            _stats.postValue(
                ProgressStats(
                    totalSessions   = repo.getTotalSessions(),
                    overallAccuracy = repo.getOverallAccuracy(),
                    avgSpeedMs      = repo.getOverallAvgSpeedMs(),
                    bestSession     = repo.getBestSession(),
                    currentStreak   = streakManager.getCurrentStreak(),
                    bestStreak      = streakManager.getBestStreak(),
                    recentSessions  = repo.getLast30Sessions().reversed()   // oldest→newest for charts
                )
            )
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repo.deleteAllSessions()
            loadStats()
        }
    }
}
