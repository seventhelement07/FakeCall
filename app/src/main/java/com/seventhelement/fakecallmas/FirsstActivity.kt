package com.seventhelement.fakecallmas

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Dao
import com.seventhelement.fakecallmas.Adapter.FirstActivityAddCallAdapter
import com.seventhelement.fakecallmas.Database.CallApp
import com.seventhelement.fakecallmas.Database.CallDao
import com.seventhelement.fakecallmas.databinding.ActivityFirsstBinding
import com.seventhelement.fakecallmas.service.FourGroundService
import kotlinx.coroutines.launch

class FirsstActivity : AppCompatActivity(), FirstActivityAddCallAdapter.OnItemClickListener  {
    lateinit var binding: ActivityFirsstBinding
    lateinit var dao: CallDao
    var name=""
    var phonenumber=""
   var selectedItem=-1
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirsstBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dao = (application as CallApp).db.dao()
        binding.addcallButton.setOnClickListener {
            val intent = Intent(this, AddCallActivity::class.java)
            startActivity(intent)
            fetch()
        }

        binding.activateButton.setOnClickListener {
            val intent=Intent(applicationContext,FourGroundService::class.java)
            startForegroundService(intent)
            //val intent=Intent(this,CallActivity::class.java)
            //startActivity(intent)
        }

   if(selectedItem==-1)
   {
       binding.activateButton.isEnabled=false
   }
        else{
       binding.activateButton.isEnabled=true
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
                val adapter = FirstActivityAddCallAdapter(callList,this@FirsstActivity)
                binding.rr.adapter = adapter
                updateArrowsVisibility()
            }
        }
    }

    override fun onPause() {
        super.onPause()
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

    override fun onItemClick(position: Int,name:String,phoneNumber:String) {
      selectedItem=position
        if(selectedItem==-1)
        {
            binding.activateButton.isEnabled=false
        }
        else{
            this.name=name
            phonenumber=phoneNumber
            binding.activateButton.isEnabled=true
        }
    }
}