package com.bodybth.gpacalculator.adapter

import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.AdapterView
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bodybth.gpacalculator.R
import com.bodybth.gpacalculator.databinding.ItemCourseBinding
import com.bodybth.gpacalculator.databinding.ItemSemesterBinding
import com.bodybth.gpacalculator.model.Course
import com.bodybth.gpacalculator.model.Semester

class SemesterAdapter(
    private val semesters: MutableList<Semester>,
    private val onDataChanged: () -> Unit
) : RecyclerView.Adapter<SemesterAdapter.SemesterVH>() {

    inner class SemesterVH(val b: ItemSemesterBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SemesterVH {
        val b = ItemSemesterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SemesterVH(b)
    }

    override fun getItemCount() = semesters.size

    override fun onBindViewHolder(holder: SemesterVH, position: Int) {
        val semester = semesters[position]
        val b = holder.b

        b.tvSemesterName.text = semester.name
        refreshCourses(b, semester)
        refreshGpa(b, semester)

        // ── Edit semester name ─────────────────────────────────────────────
        b.btnEditSemester.setOnClickListener {
            showEditSemesterDialog(it.context, semester) {
                b.tvSemesterName.text = semester.name
                refreshGpa(b, semester)
                onDataChanged()
            }
        }

        // ── Delete semester (with confirmation) ────────────────────────────
        b.btnDeleteSemester.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
            AlertDialog.Builder(it.context)
                .setTitle("Delete Semester")
                .setMessage("Delete \"${semester.name}\" and all its courses?")
                .setPositiveButton("Delete") { _, _ ->
                    val p = holder.adapterPosition
                    if (p != RecyclerView.NO_POSITION) {
                        semesters.removeAt(p)
                        notifyItemRemoved(p)
                        notifyItemRangeChanged(p, semesters.size)
                        onDataChanged()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // ── Add course ─────────────────────────────────────────────────────
        b.btnAddCourse.setOnClickListener {
            semester.courses.add(Course())
            refreshCourses(b, semester)
            refreshGpa(b, semester)
            onDataChanged()
        }
    }

    // ── Rebuild all course rows inside the semester card ───────────────────
    private fun refreshCourses(b: ItemSemesterBinding, semester: Semester) {
        b.coursesContainer.removeAllViews()
        val inflater = LayoutInflater.from(b.root.context)
        semester.courses.forEach { course ->
            val cb = ItemCourseBinding.inflate(inflater, b.coursesContainer, false)
            bindCourse(cb, course, semester, b)
            b.coursesContainer.addView(cb.root)
        }
    }

    // ── Bind a single course row ───────────────────────────────────────────
    private fun bindCourse(
        cb: ItemCourseBinding,
        course: Course,
        semester: Semester,
        semBinding: ItemSemesterBinding
    ) {
        val ctx = cb.root.context

        // Course name (inline edit)
        cb.etCourseName.setText(course.name)
        cb.etCourseName.addTextChangedListener(SimpleWatcher {
            course.name = it
            onDataChanged()
        })

        // Grade spinner
        val gradeAdapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_item, Semester.GRADES)
        gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        cb.spinnerGrade.adapter = gradeAdapter
        cb.spinnerGrade.setSelection(Semester.GRADES.indexOf(course.grade).coerceAtLeast(0))
        cb.spinnerGrade.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                course.grade = Semester.GRADES[pos]
                refreshGpa(semBinding, semester)
                onDataChanged()
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        // Credit hours
        cb.etCreditHours.setText(course.creditHours.toString())
        cb.etCreditHours.addTextChangedListener(SimpleWatcher {
            course.creditHours = it.toIntOrNull() ?: 0
            refreshGpa(semBinding, semester)
            onDataChanged()
        })

        // Weight spinner
        val weightAdapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_item, Semester.WEIGHTS)
        weightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        cb.spinnerWeight.adapter = weightAdapter
        cb.spinnerWeight.setSelection(Semester.WEIGHTS.indexOf(course.weight).coerceAtLeast(0))
        cb.spinnerWeight.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                course.weight = Semester.WEIGHTS[pos]
                refreshGpa(semBinding, semester)
                onDataChanged()
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        // Edit course (dialog)
        cb.btnEditCourse.setOnClickListener {
            showEditCourseDialog(ctx, course) {
                cb.etCourseName.setText(course.name)
                cb.spinnerGrade.setSelection(Semester.GRADES.indexOf(course.grade).coerceAtLeast(0))
                cb.etCreditHours.setText(course.creditHours.toString())
                cb.spinnerWeight.setSelection(Semester.WEIGHTS.indexOf(course.weight).coerceAtLeast(0))
                refreshGpa(semBinding, semester)
                onDataChanged()
            }
        }

        // Delete course (with confirmation)
        cb.btnDeleteCourse.setOnClickListener {
            val label = course.name.trim().ifEmpty { "this course" }
            AlertDialog.Builder(ctx)
                .setTitle("Delete Course")
                .setMessage("Delete \"$label\"?")
                .setPositiveButton("Delete") { _, _ ->
                    semester.courses.remove(course)
                    refreshCourses(semBinding, semester)
                    refreshGpa(semBinding, semester)
                    onDataChanged()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    // ── Update semester GPA label ──────────────────────────────────────────
    private fun refreshGpa(b: ItemSemesterBinding, semester: Semester) {
        val gpa = semester.calculateGpa()
        b.tvSemesterGpa.text = "${semester.name} GPA:  ${String.format("%.2f", gpa)}"
    }

    // ── Dialogs ────────────────────────────────────────────────────────────
    private fun showEditSemesterDialog(ctx: Context, semester: Semester, onSave: () -> Unit) {
        val et = EditText(ctx).apply {
            setText(semester.name)
            hint = "Semester name"
            setPadding(56, 32, 56, 32)
            setSingleLine()
        }
        AlertDialog.Builder(ctx)
            .setTitle("Edit Semester")
            .setView(et)
            .setPositiveButton("Save") { _, _ ->
                val txt = et.text.toString().trim()
                if (txt.isNotEmpty()) { semester.name = txt; onSave() }
            }
            .setNegativeButton("Cancel", null)
            .show()
        et.requestFocus()
    }

    private fun showEditCourseDialog(ctx: Context, course: Course, onSave: () -> Unit) {
        val view = LayoutInflater.from(ctx).inflate(R.layout.dialog_edit_course, null)
        val etName   = view.findViewById<EditText>(R.id.etDialogCourseName)
        val spGrade  = view.findViewById<android.widget.Spinner>(R.id.spinnerDialogGrade)
        val etCr     = view.findViewById<EditText>(R.id.etDialogCreditHours)
        val spWeight = view.findViewById<android.widget.Spinner>(R.id.spinnerDialogWeight)

        etName.setText(course.name)
        etCr.setText(course.creditHours.toString())

        val gA = ArrayAdapter(ctx, android.R.layout.simple_spinner_item, Semester.GRADES)
        gA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spGrade.adapter = gA
        spGrade.setSelection(Semester.GRADES.indexOf(course.grade).coerceAtLeast(0))

        val wA = ArrayAdapter(ctx, android.R.layout.simple_spinner_item, Semester.WEIGHTS)
        wA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spWeight.adapter = wA
        spWeight.setSelection(Semester.WEIGHTS.indexOf(course.weight).coerceAtLeast(0))

        AlertDialog.Builder(ctx)
            .setTitle("Edit Course")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                course.name        = etName.text.toString().trim()
                course.grade       = Semester.GRADES[spGrade.selectedItemPosition]
                course.creditHours = etCr.text.toString().toIntOrNull() ?: 0
                course.weight      = Semester.WEIGHTS[spWeight.selectedItemPosition]
                onSave()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ── Simple TextWatcher helper ─────────────────────────────────────────
    private class SimpleWatcher(private val onChange: (String) -> Unit) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) { onChange(s?.toString() ?: "") }
    }
}
