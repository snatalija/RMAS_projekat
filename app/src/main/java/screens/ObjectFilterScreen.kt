package com.example.projekat.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projekat.viewmodels.ObjectViewModel
import com.example.projekat.viewmodels.MyObject // Importuj iz ViewModel-a
import androidx.compose.runtime.livedata.observeAsState


@Composable
fun ObjectFilterScreen(viewModel: ObjectViewModel = viewModel()) {
    var filteredObjects by remember { mutableStateOf<List<MyObject>>(emptyList()) }
    var filterText by remember { mutableStateOf("") }

    // Prikupljanje podataka sa LiveData
    val allObjects by viewModel.filteredObjects.observeAsState(emptyList())

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // Filter text field
        TextField(
            value = filterText,
            onValueChange = {
                filterText = it
                viewModel.applyFilter(it)
            },
            label = { Text("Filter") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Lista filtriranih objekata
        LazyColumn {
            items(filteredObjects) { obj ->
                ObjectItem(obj)
            }
        }

    }
}

@Composable
fun ObjectItem(obj: MyObject) {
    // Prikaz pojedinačnog objekta
    Text(text = obj.name)
}
