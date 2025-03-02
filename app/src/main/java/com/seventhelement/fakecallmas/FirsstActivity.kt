package com.seventhelement.fakecallmas

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.seventhelement.fakecallmas.Adapter.FirstActivityAddCallAdapter
import com.seventhelement.fakecallmas.Database.CallApp
import com.seventhelement.fakecallmas.Database.CallDao
import com.seventhelement.fakecallmas.databinding.ActivityFirsstBinding
import com.seventhelement.fakecallmas.service.FourGroundService
import kotlinx.coroutines.launch

class FirsstActivity : AppCompatActivity(), FirstActivityAddCallAdapter.OnItemClickListener {

    private lateinit var binding: ActivityFirsstBinding
    private lateinit var dao: CallDao
    private lateinit var sharedPreferences: SharedPreferences
    private var selectedItem = -1
    private var name = ""
    private var phoneNumber = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirsstBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dao = (application as CallApp).db.dao()
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val callPrefs = getSharedPreferences("FakeCallPreferences", Context.MODE_PRIVATE)

        name = callPrefs.getString("name", "Unknown Caller").toString()
        phoneNumber = callPrefs.getString("phone", "Unknown Number").toString()

        selectedItem = sharedPreferences.getInt("selectedItem", -1)

        updateActivateButtonState()

        var isServiceOn = isServiceRunning(this, FourGroundService::class.java)
        binding.activateButton.text = if (isServiceOn) "De-Active" else "Active"

        binding.addcallButton.setOnClickListener {
            val intent = Intent(this, AddCallActivity::class.java)
            startActivity(intent)
            fetch()
        }

        binding.activateButton.setOnClickListener {
            handleServiceToggle()
        }

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rr.layoutManager = layoutManager
        binding.rr.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                updateArrowsVisibility()
            }
        })

        binding.leftArrow.setOnClickListener {
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            if (firstVisibleItemPosition > 0) {
                binding.rr.smoothScrollToPosition(firstVisibleItemPosition - 1)
            }
        }

        binding.rightArrow.setOnClickListener {
            val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
            if (lastVisibleItemPosition < layoutManager.itemCount - 1) {
                binding.rr.smoothScrollToPosition(lastVisibleItemPosition + 1)
            }
        }
    }

    private fun fetch() {
        lifecycleScope.launch {
            dao.fetchallTable().collect { callList ->
                val adapter = FirstActivityAddCallAdapter(callList, this@FirsstActivity, selectedItem)
                binding.rr.adapter = adapter
                updateArrowsVisibility()
            }
        }
    }

    private fun updateArrowsVisibility() {
        val layoutManager = binding.rr.layoutManager as LinearLayoutManager
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
        binding.leftArrow.visibility = if (firstVisibleItemPosition > 0) View.VISIBLE else View.GONE
        binding.rightArrow.visibility = if (lastVisibleItemPosition < layoutManager.itemCount - 1) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        fetch()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onItemClick(position: Int, name: String, phoneNumber: String) {
        selectedItem = position
        this.name = name
        this.phoneNumber = phoneNumber
        updateActivateButtonState()

        sharedPreferences.edit().putInt("selectedItem", selectedItem).apply()

       // startFakeCallService()
    }

    private fun updateActivateButtonState() {
        binding.activateButton.isEnabled = (selectedItem != -1)
    }

    private fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun handleServiceToggle() {
        val isServiceOn = isServiceRunning(this, FourGroundService::class.java)
        val intent = Intent(applicationContext, FourGroundService::class.java).apply {
            putExtra("name", name)
            putExtra("number", phoneNumber)
        }

        if (isServiceOn) {
            stopService(intent)
            binding.activateButton.text = "Active"
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(this, intent)
            } else {
                startService(intent)
            }
            binding.activateButton.text = "De-Active"
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startFakeCallService() {
        val intent = Intent(applicationContext, FourGroundService::class.java).apply {
            putExtra("name", name)
            putExtra("number", phoneNumber)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, intent)
        } else {
            startService(intent)
        }
    }
}
