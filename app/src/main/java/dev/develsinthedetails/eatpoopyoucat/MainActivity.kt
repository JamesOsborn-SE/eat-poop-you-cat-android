package dev.develsinthedetails.eatpoopyoucat

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil.setContentView
import androidx.fragment.app.DialogFragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import dev.develsinthedetails.eatpoopyoucat.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import dev.develsinthedetails.eatpoopyoucat.permissionsUtiliy.PermissionsUtility

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Displaying edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView<ActivityMainBinding>(this, R.layout.activity_main)

    }
    override fun onStart() {
        super.onStart()
        PermissionsUtility.requestPermissions(this)
        PermissionsUtility.requestBackGroundPermission(this)
    }
}
