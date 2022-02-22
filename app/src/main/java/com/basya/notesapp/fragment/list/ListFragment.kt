package com.basya.notesapp.fragment.list

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.basya.notesapp.R
import com.basya.notesapp.data.model.NoteData
import com.basya.notesapp.data.viewModelData.NotesViewModel
import com.basya.notesapp.databinding.FragmentListBinding
import com.basya.notesapp.fragment.SharedViewModels
import com.basya.notesapp.fragment.list.adapter.ListAdapter
import com.basya.notesapp.utils.hideKeyboard
import com.google.android.material.snackbar.Snackbar
import jp.wasabeef.recyclerview.animators.LandingAnimator

class ListFragment : Fragment(), SearchView.OnQueryTextListener {

    private val mNotesViewModel : NotesViewModel by viewModels()
    private val adapter : ListAdapter by lazy { ListAdapter() }
    private val mSharedViewModel: SharedViewModels by viewModels()
    private var _listBinding : FragmentListBinding? = null
    private val listBinding get() = _listBinding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _listBinding = FragmentListBinding.inflate(inflater, container, false)
        listBinding.lifecycleOwner = this
        listBinding.mSharedViewModel = mSharedViewModel

        setUpRecyclerView()

        mNotesViewModel.getAllData.observe(viewLifecycleOwner, {data ->
            mSharedViewModel.checkIfDatabaseEmpty(data)
            adapter.setData(data)
        })

        setHasOptionsMenu(true)
        hideKeyboard(requireActivity())
        return listBinding.root
    }

    private fun setUpRecyclerView(){
        listBinding.rvList.adapter = adapter
        listBinding.rvList.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        listBinding.rvList.itemAnimator = LandingAnimator().apply {
            addDuration = 300
        }
        swipeToDelete(listBinding.rvList)
    }

    private fun swipeToDelete(recyclerView: RecyclerView) {
     val swipeToDeleteCallback = object : SwipeToDelete(){
         override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
             val deletedItem = adapter.dataList[viewHolder.adapterPosition]
             mNotesViewModel.deleteData(deletedItem)
             adapter.notifyItemRemoved(viewHolder.adapterPosition)
             restoreDeletedData(viewHolder.itemView, deletedItem)

         }
     }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private  fun restoreDeletedData(view: View, deletedItem: NoteData){
        val snackbar = Snackbar.make(
            view,"Deleted '${deletedItem.title}'",
            Snackbar.LENGTH_SHORT
        )
        snackbar.setAction("Undo"){
            mNotesViewModel.insertData(deletedItem)
        }
        snackbar.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fragment_menu, menu)
        val search = menu.findItem(R.id.menu_search)
        val searchView = search.actionView as? SearchView
        searchView?.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(this)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_delete_all -> confirmRemoveAll()
            R.id.menu_priority_high -> mNotesViewModel.sortByHighPriority.observe(this,{
                adapter.setData(it)
            })
            R.id.menu_priority_low -> mNotesViewModel.sortByLowPriority.observe(this,{
                adapter.setData(it)
            })
        }
        return super.onOptionsItemSelected(item)
    }

    private fun confirmRemoveAll(){
        AlertDialog.Builder(requireContext())
            .setTitle("Delete All ?")
            .setMessage("Are You Sure Want To Remove All ?")
            .setPositiveButton("yes"){_, _ ->
                mNotesViewModel.deleteAllData()
                Toast.makeText(requireContext(), "Successfully Remove All",
                Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("NO", null)
            .create()
            .show()
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if(query != null){
            searchThroughDatabase(query)
        }
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        if (query != null){
            searchThroughDatabase(query)
        }
        return true
    }

    private fun searchThroughDatabase(query: String?){
        val searchQuery = "%$query"

        mNotesViewModel.searchDatabase(searchQuery).observe(this, {
            list -> list.let { adapter.setData(it) }
        })
    }

    override fun onDestroy() {
        _listBinding = null
        super.onDestroy()
    }
}