package com.example.serbisyo_it342_g3

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.serbisyo_it342_g3.fragments.AddressFragment
import com.example.serbisyo_it342_g3.fragments.BookingHistoryFragment
import com.example.serbisyo_it342_g3.fragments.ChangePasswordFragment
import com.example.serbisyo_it342_g3.fragments.ProfileFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ProfileManagementActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    
    private var userId: Long = 0
    private var token: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_management)
        
        // Set up toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "Profile Management"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Get user data from SharedPreferences
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        token = sharedPref.getString("token", "") ?: ""
        val userIdStr = sharedPref.getString("userId", "0") ?: "0"
        userId = userIdStr.toLongOrNull() ?: 0
        
        // Initialize views
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        
        // Set up the ViewPager with the adapter
        val pagerAdapter = ProfilePagerAdapter(supportFragmentManager, lifecycle)
        viewPager.adapter = pagerAdapter
        
        // Connect the TabLayout with the ViewPager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Profile"
                1 -> "Address"
                2 -> "Bookings"
                3 -> "Password"
                else -> null
            }
        }.attach()
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    
    inner class ProfilePagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
        FragmentStateAdapter(fragmentManager, lifecycle) {
        
        override fun getItemCount(): Int = 4
        
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> {
                    // Profile Fragment
                    val fragment = ProfileFragment()
                    val args = Bundle()
                    args.putLong("userId", userId)
                    args.putString("token", token)
                    fragment.arguments = args
                    fragment
                }
                1 -> {
                    // Address Fragment
                    val fragment = AddressFragment()
                    val args = Bundle()
                    args.putLong("userId", userId)
                    args.putString("token", token)
                    fragment.arguments = args
                    fragment
                }
                2 -> {
                    // Booking History Fragment
                    val fragment = BookingHistoryFragment()
                    val args = Bundle()
                    args.putLong("userId", userId)
                    args.putString("token", token)
                    fragment.arguments = args
                    fragment
                }
                3 -> {
                    // Change Password Fragment
                    val fragment = ChangePasswordFragment()
                    val args = Bundle()
                    args.putLong("userId", userId)
                    args.putString("token", token)
                    fragment.arguments = args
                    fragment
                }
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }
    }
} 