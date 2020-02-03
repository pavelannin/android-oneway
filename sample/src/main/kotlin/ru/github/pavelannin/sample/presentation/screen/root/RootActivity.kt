package ru.github.pavelannin.sample.presentation.screen.root

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_root.*
import ru.github.pavelannin.sample.R

class RootActivity : AppCompatActivity(R.layout.activity_root), NavHost {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityRootToolbar.setupWithNavController(navController)
    }

    override fun getNavController(): NavController {
        return findNavController(R.id.activityRootNavigationHost)
    }
}
