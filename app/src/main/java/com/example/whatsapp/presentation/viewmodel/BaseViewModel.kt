package com.example.whatsapp.presentation.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.animation.core.snap
import androidx.lifecycle.ViewModel
import com.example.whatsapp.model.Message
import com.example.whatsapp.presentation.chat_box.ChatListModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okio.IOException
import java.io.ByteArrayInputStream
import java.io.InputStream

class BaseViewModel : ViewModel() {

    fun searchUserByPhoneNumber(phoneNumber: String, callback: (ChatListModel?) -> Unit) {

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {  // to check if current user has been obtained

            Log.e("BaseViewModel", "User is not authenticated")
            callback(null)
            return
        }

        val databaseReference = FirebaseDatabase.getInstance()
            .getReference("users") // giving the node of users at firebase db
        databaseReference.orderByChild("phonNumber").equalTo(phoneNumber)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    // checking if snapshots exists
                    if (snapshot.exists()) {

                        val user = snapshot.children.first().getValue(ChatListModel::class.java)
                        callback(user)

                    } else {

                        callback(null)
                    }
                }

                // to cancel the search midway
                override fun onCancelled(error: DatabaseError) {

                    Log.e(
                        "BaseViewModel",
                        "Error fetching User: ${error.message}, Details : ${error.details}"
                    )
                    callback(null)
                }

            })

    }

    fun getChatForUser(userId: String, callback: (List<ChatListModel>) -> Unit) {

        val chatRef = FirebaseDatabase.getInstance()
            .getReference("users/$userId/chats")  // will search on this path in db

        chatRef.orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    val chatList = mutableListOf<ChatListModel>()

                    for (childSnapShot in snapshot.children) {   // storing chats from chatList

                        val chat = childSnapShot.getValue(ChatListModel::class.java)

                        // chat should not be null
                        if (chat != null) {
                            chatList.add(chat)
                        }

                    }
                    callback(chatList)
                }

                override fun onCancelled(error: DatabaseError) {

                    Log.e("BaseViewModel", " Error fetching user chats : ${error.message}")
                    callback(emptyList())  // for null
                }
            }
            )
    }

    private val _chatList = MutableStateFlow<List<ChatListModel>>(emptyList())
    val chatList = _chatList.asStateFlow()

    init {
        LoadChatData()
    }

    private fun LoadChatData() {

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid  // if null, pass uid

        if (currentUserId != null) {

            val chatRef = FirebaseDatabase.getInstance().getReference("chats")

            chatRef.orderByChild("userId")
                .equalTo(currentUserId)  // checking if the userId from the db and current is same or not
                .addValueEventListener(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val chatList = mutableListOf<ChatListModel>()

                        for (childSnapshot in snapshot.children) {

                            val chat =
                                childSnapshot.getValue(ChatListModel::class.java)  // Taking value from ChatList and  converting it in java

                            if (chat != null) {

                                chatList.add(chat)
                            }

                        }

                        _chatList.value = chatList  // updating the _chatList MutableList
                    }

                    override fun onCancelled(error: DatabaseError) {

                        Log.e("BaseViewModel", " Error fetching user chats : ${error.message}")

                    }
                })
        }
    }

    fun addChat(newChat: ChatListModel) {

        // taking currentUserId to identify where to update the data in db
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId != null) {

            val newChatRef = FirebaseDatabase.getInstance().getReference("chats").push()
            val chatWithUser = newChat.copy(currentUserId)

            newChatRef.setValue(chatWithUser)
                .addOnSuccessListener {  // setting the value of newChatRef in db so to add every new chat in chatWithUser

                    Log.d("BaseViewModel", "chat added successfully to firebase")

                }.addOnFailureListener { exception ->

                    Log.e("BaseViewModel", "Failed to add chat: ${exception.message}")
                }

        } else {  // if user is not authenticated

            Log.e("BaseViewModel", "No user is authenticated")
        }
    }

    // to take reference from db
    private val databaseReference = FirebaseDatabase.getInstance().reference

    fun sendMessage(senderPhoneNumber: String, receiverPhoneNumber: String, messageText: String) {

        val messageId = databaseReference.push().key
            ?: return  // pushing message into database and return if failed or in case of null
        val message = Message(
            senderPhoneNumber = senderPhoneNumber,
            message = messageText,
            timeStamp = System.currentTimeMillis()
        )

        // creating a model through which msg will be sent or received
        // sending it to db for sender
        databaseReference.child("messages")  // message is a node and so is the child
            .child(senderPhoneNumber)
            .child(receiverPhoneNumber)
            .child(messageId)            // sending msg in messageId
            .setValue(message)

        // sending it to db for receiver
        databaseReference.child("messages") // each child is creating a node in node
            .child(receiverPhoneNumber)
            .child(senderPhoneNumber)
            .child(messageId)
            .setValue(message)
    }

    fun getMessage(
        senderPhoneNumber: String,
        receiverPhoneNumber: String,
        onNewMessage: (Message) -> Unit
    ) {

        val messageRef = databaseReference.child("messages")
            .child(senderPhoneNumber)
            .child(receiverPhoneNumber)

        messageRef.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                val message = snapshot.getValue(Message::class.java)

                if (message != null) {

                    onNewMessage(message)
                }
            }

            override fun onChildChanged(
                snapshot: DataSnapshot,
                previousChildName: String?
            ) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(
                snapshot: DataSnapshot,
                previousChildName: String?
            ) {
            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    // to fetch the last/  most recent message to show on home screen
    fun fetchLastMessageForChat(
        senderPhoneNumber: String,
        receiverPhoneNumber: String,
        onLastMessageFetched: (String, String) -> Unit
    ) {

        val chatRef = FirebaseDatabase.getInstance().reference
            .child("message")
            .child("senderPhoneNumber")
            .child("receiverPhoneNumber")

        // using timeStamp to get the last chat   // using limitToLast so limiting till which chat from last we have to fetch
        chatRef.orderByChild("timestamp").limitToLast(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()) {  // snapshots hold our message so we check if it exists or not

                        val lastMessage =
                            snapshot.children.firstOrNull()?.child("message")?.value as? String

                        val timestamp =
                            snapshot.children.firstOrNull()?.child("timestamp")?.value as? String

                        onLastMessageFetched(
                            lastMessage ?: "No message",
                            timestamp ?: "--:--"
                        )   // if last message is empty
                    } else {

                        onLastMessageFetched("No message", "--:--")
                    }

                }

                override fun onCancelled(error: DatabaseError) {

                    // we can use Log also
                    onLastMessageFetched("No message", "--:--")
                }

            })

    }

    fun loadChatList(
        currentUserPhoneNumber: String,
        onChatListLoaded: (List<ChatListModel>) -> Unit
    ) {

        val chatList = mutableListOf<ChatListModel>()
        val chatRef = FirebaseDatabase.getInstance().reference
            .child("chats")
            .child(currentUserPhoneNumber)

        chatRef.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {

                    snapshot.children.forEach { child ->    // taking a key as child

                        val phoneNumber = child.key ?: return@forEach
                        val name = child.child("name").value as? String ?: "Unknown"  // using child we are fetching name from db

                        val image = child.child("image").value as? String

                        // to convert image back to og form
                        val profileImageBitmap = image?.let {decodeBase64toBitmap(it)}

                        fetchLastMessageForChat(currentUserPhoneNumber, phoneNumber){ lastMessage, time ->

                            chatList.add(
                                ChatListModel(
                                    name = name,
                                    image = profileImageBitmap as Int?,
                                    message = lastMessage,
                                    time = time
                                )
                            )

                            //
                            if (chatList.size == snapshot.childrenCount.toInt()){
                                onChatListLoaded(chatList)
                            }

                        }

                    }

                }else{

                    onChatListLoaded(emptyList())   // if no chatList is there passing emptyList
                }

            }

            override fun onCancelled(error: DatabaseError) {
            // while cancelling no chat list should be obtained so passing emptyList

                onChatListLoaded(emptyList())
            }

        })

    }

    private fun decodeBase64toBitmap(base64Image: String): Bitmap?{

        return try{

            val decodedByte = Base64.decode(base64Image, Base64.DEFAULT)

            BitmapFactory.decodeByteArray(decodedByte,0,decodedByte.size)

        }catch (e : IOException){

            null
        }

    }

    fun base64toBitmap(base64String: String): Bitmap?{

        return try {

            val decodedByte = Base64.decode(base64String, Base64.DEFAULT)
            val inputStream: InputStream = ByteArrayInputStream(decodedByte)

            BitmapFactory.decodeStream(inputStream)

        }catch (e : IOException){
            null
        }
    }

}