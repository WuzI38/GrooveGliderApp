package edu.put.grooveglider.activities

import edu.put.grooveglider.internet.LoadingViewModel
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import edu.put.grooveglider.R

// Display connection status, show loading screen if the connection cannot be established
class LoadingActivity : AppCompatActivity() {

    private lateinit var viewModel: LoadingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        viewModel = ViewModelProvider(this)[LoadingViewModel::class.java]

        viewModel.connectionState.observe(this) { isConnected ->
            if (isConnected) {
                val intent = Intent(this, MainActivity::class.java)
                Log.d("ConRes", "Connected")
                startActivity(intent)
                finish()
            } else {
                Log.d("ConRes", "Trying to connect")
                viewModel.connect()
            }
        }

        viewModel.connect()
    }
}