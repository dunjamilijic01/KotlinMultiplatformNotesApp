package com.example.notesapp.android.note_list

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteDataSource
import com.example.notesapp.domain.note.SearchNotes
import com.example.notesapp.domain.time.DateTimeUtil
import com.example.notesapp.presentation.pink
import com.example.notesapp.presentation.purple
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private  val noteDataSource: NoteDataSource,
    private val savedStateHandle: SavedStateHandle
):ViewModel() {

    private val searchNotes = SearchNotes()

    private val notes= savedStateHandle.getStateFlow("notes", emptyList<Note>())
    private val searchText = savedStateHandle.getStateFlow("searchText","")
    private val isSearchActive = savedStateHandle.getStateFlow("isSearchActive",false)

    val state = combine(notes,searchText,isSearchActive){notes, searchtext,isSearchActive ->
        NoteListState(
            notes = searchNotes.search(notes,searchtext),
            searchText = searchtext,
            isSearchActive = isSearchActive
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),NoteListState())

    fun loadNotes(){
        viewModelScope.launch {
            savedStateHandle["notes"]=noteDataSource.getAllNotes()
        }
    }

    fun onSearchTextChange(text:String) {
        savedStateHandle["searchText"]=text
    }

    fun onToggleSearch(){
        savedStateHandle["isSearchActive"]=!isSearchActive.value
        if(!isSearchActive.value)
            savedStateHandle["searchText"]=""
    }

    fun deleteNoteById(id:Long)
    {
        viewModelScope.launch {
            noteDataSource.deleteNote(id)
            loadNotes()
        }
    }

}