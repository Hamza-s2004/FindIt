package com.example.findit.data.repository

import com.example.findit.data.model.Item
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseRepository {

    private val db = FirebaseFirestore.getInstance()
    private val itemsRef = db.collection("items")

    suspend fun addItem(item: Item) {
        itemsRef.add(item).await()
    }

    suspend fun getItems(): List<Item> {
        val snapshot = itemsRef.get().await()

        return snapshot.documents.mapNotNull { doc ->
            val item = doc.toObject(Item::class.java)
            item?.apply {
                id = doc.id.hashCode().toLong()
                remoteId = doc.id   // 🔥 IMPORTANT
            }
        }
    }

    fun listenItems(callback: (List<Item>) -> Unit) {
        itemsRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {

                val items = snapshot.documents.mapNotNull { doc ->
                    val item = doc.toObject(Item::class.java)
                    item?.apply {
                        id = doc.id.hashCode().toLong()
                        remoteId = doc.id   // 🔥 IMPORTANT
                    }
                }

                callback(items)
            }
        }
    }

    // 🔥 REAL DELETE
    suspend fun deleteItem(remoteId: String) {
        itemsRef.document(remoteId).delete().await()
    }
}