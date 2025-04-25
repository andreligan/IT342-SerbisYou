package com.example.serbisyo_it342_g3

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.serbisyo_it342_g3.fragments.ChangePasswordFragment
import com.example.serbisyo_it342_g3.fragments.ServiceProviderAddressFragment
import com.example.serbisyo_it342_g3.fragments.ServiceProviderBusinessDetailsFragment
import com.example.serbisyo_it342_g3.fragments.ServiceProviderProfileFragment
import com.example.serbisyo_it342_g3.fragments.ServiceProviderScheduleFragment
import com.example.serbisyo_it342_g3.fragments.ServiceProviderServicesFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ServiceProviderProfileManagementActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    
    private var providerId: Long = 0
    private var userId: Long = 0
    private var token: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_provider_profile_management)
        
        // Set up toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "Provider Profile"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Get user data from SharedPreferences
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        token = sharedPref.getString("token", "") ?: ""
        val userIdStr = sharedPref.getString("userId", "0") ?: "0"
        userId = userIdStr.toLongOrNull() ?: 0
        // Try to get providerId from SharedPreferences, will be updated if not found
        val providerIdStr = sharedPref.getString("providerId", "0") ?: "0"
        providerId = providerIdStr.toLongOrNull() ?: 0
        
        // Initialize views
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        
        // Set up the ViewPager with the adapter
        val pagerAdapter = ProviderProfilePagerAdapter(supportFragmentManager, lifecycle)
        viewPager.adapter = pagerAdapter
        
        // Connect the TabLayout with the ViewPager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Profile"
                1 -> "Address"
                2 -> "Business Details"
                3 -> "My Services"
                4 -> "Schedule"
                5 -> "Password"
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
    
    inner class ProviderProfilePagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
        FragmentStateAdapter(fragmentManager, lifecycle) {
        
        override fun getItemCount(): Int = 6
        
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> {
                    // Profile Fragment
                    val fragment = ServiceProviderProfileFragment()
                    val args = Bundle()
                    args.putLong("userId", userId)
                    args.putString("token", token)
                    args.putLong("providerId", providerId)
                    fragment.arguments = args
                    fragment
                }
                1 -> {
                    // Address Fragment
                    val fragment = ServiceProviderAddressFragment()
                    val args = Bundle()
                    args.putLong("userId", userId)
                    args.putString("token", token)
                    args.putLong("providerId", providerId)
                    fragment.arguments = args
                    fragment
                }
                2 -> {
                    // Business Details Fragment
                    val fragment = ServiceProviderBusinessDetailsFragment()
                    val args = Bundle()
                    args.putLong("userId", userId)
                    args.putString("token", token)
                    args.putLong("providerId", providerId)
                    fragment.arguments = args
                    fragment
                }
                3 -> {
                    // My Services Fragment
                    val fragment = ServiceProviderServicesFragment()
                    val args = Bundle()
                    args.putLong("userId", userId)
                    args.putString("token", token)
                    args.putLong("providerId", providerId)
                    fragment.arguments = args
                    fragment
                }
                4 -> {
                    // Schedule Fragment
                    val fragment = ServiceProviderScheduleFragment()
                    val args = Bundle()
                    args.putLong("userId", userId)
                    args.putString("token", token)
                    args.putLong("providerId", providerId)
                    fragment.arguments = args
                    fragment
                }
                5 -> {
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