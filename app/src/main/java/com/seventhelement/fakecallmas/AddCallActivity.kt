package com.seventhelement.fakecallmas

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.seventhelement.fakecallmas.Database.CallApp
import com.seventhelement.fakecallmas.Database.CallEntity
import com.seventhelement.fakecallmas.databinding.ActivityAddCallBinding
import kotlinx.coroutines.launch

class AddCallActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddCallBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityAddCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val dao=(application as CallApp).db.dao()
        binding.button.setOnClickListener {
            lifecycleScope.launch {
                dao.insert(CallEntity(name=binding.etName.text.toString(), number = binding.etCallerNumber.text.toString().toLong()))
                finish()
            }
        }
    }
}