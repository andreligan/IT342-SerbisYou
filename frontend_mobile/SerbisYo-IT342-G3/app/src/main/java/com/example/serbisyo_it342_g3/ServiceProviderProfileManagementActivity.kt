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
    
    // Save current tab to handle lifecycle events
    private var currentTab = 0

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
        
        // Fix userId retrieval using try-catch
        userId = try {
            // Try to get as Long first (new format)
            sharedPref.getLong("userId", 0)
        } catch (e: ClassCastException) {
            // If that fails, try the String format (old format) and convert
            val userIdStr = sharedPref.getString("userId", "0")
            userIdStr?.toLongOrNull() ?: 0
        }
        
        // Fix providerId retrieval using try-catch
        providerId = try {
            // Try to get as Long first (new format)
            sharedPref.getLong("providerId", 0)
        } catch (e: ClassCastException) {
            // If that fails, try the String format (old format) and convert
            val providerIdStr = sharedPref.getString("providerId", "0")
            providerIdStr?.toLongOrNull() ?: 0
        }
        
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
        
        // Check if we need to show a specific tab (from intent)
        val selectedTab = intent.getIntExtra("SELECTED_TAB", -1)
        if (selectedTab in 0..5) {
            viewPager.setCurrentItem(selectedTab, false)
            currentTab = selectedTab
        }
        
        // Save the tab position when it changes
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentTab = position
            }
        })
        
        // If restoring from saved state, get the previously selected tab
        if (savedInstanceState != null) {
            currentTab = savedInstanceState.getInt("CURRENT_TAB", 0)
            viewPager.setCurrentItem(currentTab, false)
        }
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the current tab position to restore it later
        outState.putInt("CURRENT_TAB", currentTab)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    
    override fun onBackPressed() {
        if (currentTab != 0) {
            // If not on the first tab, go to the first tab instead of closing the activity
            viewPager.setCurrentItem(0, true)
        } else {
            super.onBackPressed()
        }
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
                    val fragment = ServiceProviderServicesFragment.newInstance(userId, token, providerId)
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
    
    companion object {
        const val SERVICES_TAB = 3
    }
}