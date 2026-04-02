package com.example.whatsapp.presentation.chatscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.whatsapp.data.session.ChatSession

@Composable
fun ChatScreen(
    phoneNumber: String,
    chatViewModel: ChatViewModel = viewModel()
) {
    var input by remember { mutableStateOf("") }
    val messages by chatViewModel.messages.collectAsState()
    val connectionStatus by chatViewModel.connectionStatus.collectAsState()
    val myId = ChatSession.userId
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(phoneNumber) {
        chatViewModel.openChat(phoneNumber)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(12.dp)
    ) {
        Text(
            text = "Chat with $phoneNumber",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Status: $connectionStatus",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                val isMine = message.senderId == myId
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        modifier = Modifier.widthIn(max = 280.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isMine) Color(0xFFFFE2C0) else Color.White
                        )
                    ) {
                        Text(
                            text = message.content,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = Color.Black
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, end = 10.dp, bottom = 6.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = message.timestamp,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            if (isMine) {
                                val tick = when (message.status) {
                                    DeliveryStatus.SENT -> " ✓"
                                    DeliveryStatus.DELIVERED -> " ✓✓"
                                    DeliveryStatus.RECEIVED -> ""
                                }
                                Text(
                                    text = tick,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF4A6572)
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onFocusChanged { state ->
                        if (state.isFocused) {
                            keyboardController?.show()
                        }
                    },
                placeholder = { Text("Type a message") }
            )
            Button(
                onClick = {
                    chatViewModel.sendMessage(input)
                    input = ""
                    keyboardController?.show()
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Send")
            }
        }
    }
}






