package com.example.appleonandroidauth

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appleonandroidauth.databinding.ActivityAppleAuthBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.oAuthProvider
import com.google.firebase.ktx.Firebase
import java.util.ArrayList

class AppleAuthActivity: AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private var _binding: ActivityAppleAuthBinding? = null
    private val binding: ActivityAppleAuthBinding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAppleAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Set up button click listeners
        binding.signInButton.setOnClickListener { signIn() }
        binding.signOutButton.setOnClickListener {
            auth.signOut()
            updateUI(null)
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)

        // Look for a pending auth result
        val pending = auth.pendingAuthResult
        if (pending != null) {
            pending.addOnSuccessListener { authResult ->
                Log.d(TAG, "checkPending:onSuccess:$authResult")
                updateUI(authResult.user)
            }.addOnFailureListener { e ->
                Log.w(TAG, "checkPending:onFailure", e)
            }
        } else {
            Log.d(TAG, "checkPending: null")
        }
    }

    private fun signIn() {
        // Could add custom scopes here
        val customScopes = ArrayList<String>()

        auth.startActivityForSignInWithProvider(
            this,
            oAuthProvider("apple.com", auth) {
                scopes = customScopes
            },
        )
            .addOnSuccessListener { authResult ->
                Log.d(TAG, "activitySignIn:onSuccess:${authResult.user}")
                updateUI(authResult.user)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "activitySignIn:onFailure", e)
                Toast.makeText(
                    this,
                    getString(R.string.error_sign_in_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            binding.status.text = getString(R.string.generic_status_fmt, user.displayName, user.email)
            binding.detail.text = getString(R.string.firebase_status_fmt, user.uid)

            binding.signOutButton.visibility = View.VISIBLE
        } else {
            binding.status.setText(R.string.signed_out)
            binding.detail.text = null

            binding.signOutButton.visibility = View.GONE
        }
    }

    companion object {
        val TAG = AppleAuthActivity::class.simpleName
    }
}