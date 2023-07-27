package com.maverkick.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/** Basic interface that wraps the database connection
 *  implementation
 **/
interface IDatabaseService {
    val db: FirebaseFirestore
}

/** The main class that is responsible for the Firebase Firestore
 * connection **/
class FirebaseService : IDatabaseService {
    override val db = Firebase.firestore
}