package edu.put.grooveglider.activities

import edu.put.grooveglider.internet.TCPHandler
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import edu.put.grooveglider.R
import edu.put.grooveglider.databinding.ActivityMainBinding
import edu.put.grooveglider.internet.Constants
import edu.put.grooveglider.internet.UDPHandler
import edu.put.grooveglider.internet.UDPHandler.connectMulticast
import edu.put.grooveglider.internet.UDPHandler.receiveMulticast
import edu.put.grooveglider.music.MusicPlayer
import edu.put.grooveglider.ui.SharedViewModel
import kotlinx.coroutines.launch
import java.io.IOException

class MainActivity : AppCompatActivity() { // java.lang.IllegalStateException: The number of released permits cannot be greater than 1

    private lateinit var binding: ActivityMainBinding

    private val sharedViewModel: SharedViewModel by viewModels()

    private var pauseState: String = "Pause"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Bottom navigation setup
        val navView: BottomNavigationView = binding.navView

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        // App Bar
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_songs, R.id.navigation_server, R.id.navigation_upload
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.itemIconTintList = null

        // Add functionality to buttons
        val pauseButton: ImageButton = findViewById(R.id.pause)
        pauseButton.setOnClickListener {
            lifecycleScope.launch {
                TCPHandler.send(pauseState)
            }
            if(pauseState == "Pause") {
                pauseState = "Play"
                pauseButton.setImageResource(R.drawable.play)
            }
            else {
                pauseState = "Pause"
                pauseButton.setImageResource(R.drawable.pause)
            }
        }

        val nextButton: ImageButton = findViewById(R.id.next)
        nextButton.setOnClickListener {
            lifecycleScope.launch {
                TCPHandler.send("Next")
            }
        }

        val prevButton: ImageButton = findViewById(R.id.previous)
        prevButton.setOnClickListener {
            lifecycleScope.launch {
                TCPHandler.send("Previous")
            }
        }

        // Init TCP
        lifecycleScope.launch {
            TCPHandler.receive(sharedViewModel)
        }

        // Init multicast
        lifecycleScope.launch {
            connectMulticast(applicationContext)
            MusicPlayer.start()
            receiveMulticast(Constants.IGNORE_UNORDERED)
        }

        // In theory this should prevent some non essential errors
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                try {
                    UDPHandler.close()
                    TCPHandler.close()
                } catch (e: IOException) { /* failed */ }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        UDPHandler.close()
        TCPHandler.close()
    }
}

