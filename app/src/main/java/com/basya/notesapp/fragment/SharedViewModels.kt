package com.basya.notesapp.fragment

import android.app.Application
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.basya.notesapp.R
import com.basya.notesapp.data.model.NoteData
import com.basya.notesapp.data.model.Priority

class SharedViewModels (application: Application) : AndroidViewModel(application) {

    val emptyDatabase : MutableLiveData<Boolean> = MutableLiveData(false)

    fun checkIfDatabaseEmpty(noteData: List<NoteData>) {
        emptyDatabase.value = noteData.isEmpty()
    }

    //Membuat variable untuk mengubah warna dari priority
    val listener: AdapterView.OnItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            when (position) {
                0 -> {
                    (parent?.getChildAt(0) as TextView).setTextColor(
                        ContextCompat.getColor(application,
                            R.color.red))
                }
                1 -> {
                    (parent?.getChildAt(0) as TextView).setTextColor(
                        ContextCompat.getColor(application,
                            R.color.yellow))
                }
                2 -> {
                    (parent?.getChildAt(0) as TextView).setTextColor(
                        ContextCompat.getColor(application,
                            R.color.green))
                }
            }
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {

        }

    }

    //Membuat fungsi untuk verifikasi data dari user
    fun verifyDataFromUser(title:String, desc:String) : Boolean {
        return if (TextUtils.isEmpty(title) || TextUtils.isEmpty(desc)) {
            false
        } else !(title.isEmpty() || desc.isEmpty())
    }

    //membuat fungsi untuk parse priority
    fun parsePriority (priority: String) : Priority {
        return when (priority) {
            "High Priority" -> {
                Priority.HIGH
            }
            "Medium Priority" -> {
                Priority.MEDIUM
            }
            "Low Priority" -> {
                Priority.LOW
            }
            else -> {
                Priority.LOW
            }
        }
    }
}
