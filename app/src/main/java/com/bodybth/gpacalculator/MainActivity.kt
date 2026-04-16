package com.bodybth.gpacalculator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bodybth.gpacalculator.adapter.SemesterAdapter
import com.bodybth.gpacalculator.databinding.ActivityMainBinding
import com.bodybth.gpacalculator.model.Semester
import com.bodybth.gpacalculator.util.DataManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: SemesterAdapter
    private val semesters = mutableListOf<Semester>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        // Load persisted semesters
        semesters.addAll(DataManager.load(this))

        adapter = SemesterAdapter(semesters) {
            updateCumulativeGpa()
            DataManager.save(this, semesters)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }

        binding.btnAddSemester.setOnClickListener {
            val n = semesters.size + 1
            semesters.add(Semester(name = "Semester $n"))
            adapter.notifyItemInserted(semesters.lastIndex)
            binding.recyclerView.scrollToPosition(semesters.lastIndex)
            updateCumulativeGpa()
            DataManager.save(this, semesters)
        }

        updateCumulativeGpa()
    }

    private fun updateCumulativeGpa() {
        if (semesters.isEmpty()) { binding.gaugeView.setGpa(0.0); return }
        var totalPts = 0.0
        var totalCr  = 0
        for (sem in semesters) {
            for (c in sem.courses) {
                val pts = (Semester.gradeToPoints(c.grade) + Semester.weightBonus(c.weight)).coerceAtMost(4.0)
                totalPts += pts * c.creditHours
                totalCr  += c.creditHours
            }
        }
        val cumulative = if (totalCr == 0) 0.0 else (totalPts / totalCr).coerceIn(0.0, 4.0)
        binding.gaugeView.setGpa(cumulative)
    }

    override fun onPause() {
        super.onPause()
        DataManager.save(this, semesters)
    }

    override fun onStop() {
        super.onStop()
        DataManager.save(this, semesters)
    }
}
