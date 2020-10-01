package com.jabirdeveloper.tinderswipe

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.database.*
import com.google.firebase.functions.HttpsCallableResult
import com.google.firebase.functions.ktx.functions
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import com.hanks.htextview.base.AnimationListener
import com.hanks.htextview.base.HTextView
import com.hanks.htextview.line.LineTextView
import com.jaredrummler.android.widget.AnimatedSvgView
import java.util.*

class First_Activity : AppCompatActivity() {
    private var firebaseAuthStateListener: AuthStateListener? = null
    private var mAuth: FirebaseAuth? = null
    private var usersDb: DatabaseReference? = null
    private val plus: SwitchpageActivity? = SwitchpageActivity()
    private var mContext: Context? = null
    //private var functions = Firebase.functions
    private var mLocationManager: LocationManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_)
        MobileAds.initialize(this) {}
        mContext = applicationContext
        mAuth = FirebaseAuth.getInstance()
        firebaseAuthStateListener = AuthStateListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val svgView: AnimatedSvgView = findViewById(R.id.animated_svg_view)
                svgView.start()
                usersDb = FirebaseDatabase.getInstance().reference.child("Users")
                usersDb!!.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.child(mAuth!!.currentUser!!.uid).child("sex").exists()) {
                            pushToken()
                        } else {
                            mAuth!!.signOut()
                            val intent = Intent(this@First_Activity, ChooseLoginRegistrationActivity::class.java)
                            startActivity(intent)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            } else {
                val intent = Intent(this@First_Activity, ChooseLoginRegistrationActivity::class.java)
                startActivity(intent)
                finish()
                return@AuthStateListener
            }
        }
        mLocationManager = this@First_Activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!mLocationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showGPSDisabledDialog()
        } else if (ActivityCompat.checkSelfPermission(this@First_Activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this@First_Activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@First_Activity, arrayOf<String?>(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.INTERNET
            ), 1)
        } else mAuth!!.addAuthStateListener(firebaseAuthStateListener!!)
        /*hTextView = findViewById(R.id.textview)
        hTextView!!.setAnimationListener(SimpleAnimationListener(this@First_Activity))
        hTextView!!.animateText("Welcome to my world")*/
    }

    private fun pushToken() {
        FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        return@OnCompleteListener
                    }
                    val token = task.result?.token
                    FirebaseDatabase.getInstance().reference.child("Users").child(mAuth!!.currentUser!!.uid).child("token").setValue(token)
                    val intent = Intent(this@First_Activity, SwitchpageActivity::class.java)
                    startActivity(intent)
                    finish()
                })
    }

    /*fun getUnreadFunction(): Task<HttpsCallableResult> {
        val data = hashMapOf(
                "uid" to "test"
        )
        return functions
                .getHttpsCallable("getUnreadChat")
                .call(data)
                .addOnSuccessListener { task ->
                    val data = task.data as Map<*, *>
                    Log.d("testGetUnreadFunction", data.toString())
                }
                .addOnFailureListener {
                    Log.d("testGetUnreadFunction", "error")
                }
    }*/
    private fun showGPSDisabledDialog() {
        val builder = AlertDialog.Builder(this@First_Activity)
        builder.setTitle(R.string.GPS_Disabled)
        builder.setMessage(R.string.GPS_open)
        builder.setPositiveButton(R.string.open_gps) { _, _ -> startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0) }.setNegativeButton(R.string.report_close) { dialog, which ->
            val intent = Intent(this@First_Activity, ShowGpsOpen::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            finish()
            startActivity(intent)
        }
        val mGPSDialog: Dialog = builder.create()
        mGPSDialog.window!!.setBackgroundDrawable(ContextCompat.getDrawable(this@First_Activity, R.drawable.myrect2))
        mGPSDialog.show()
    }
    private var countNumberChat: Int? = 0

    /*var nameCaution: MutableList<String?>? = ArrayList()
    var valueCaution: MutableList<Int?>? = ArrayList()
    private var sumReported = 0
    private fun checkReport() {
        val reportDb = FirebaseDatabase.getInstance().reference.child("Users").child(mAuth!!.currentUser!!.uid).child("Report")
        reportDb.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                    val myUnread = getSharedPreferences("TotalMessage", Context.MODE_PRIVATE)
                    val editorRead = myUnread.edit()
                    editorRead.putInt("total", countNumberChat!!.toInt())
                    editorRead.apply()
                    val intent = Intent(this@First_Activity, SwitchpageActivity::class.java)
                    var sumReport: Int? = 0
                    if (dataSnapshot.exists()) {
                        for (dd in dataSnapshot.children) {
                            sumReport = Integer.valueOf(dataSnapshot.child(dd.key.toString()).value.toString())
                            nameCaution?.add(dd.key)
                            valueCaution?.add(sumReport)
                        }
                        if (sumReport != 0) {
                            intent.putExtra("warning", nameCaution as ArrayList<String?>?)
                            intent.putExtra("warning_value", valueCaution as ArrayList<Int?>?)
                        }
                        intent.putExtra("first", countNumberChat.toString())
                        startActivity(intent)
                        finish()
                    } else {
                        intent.putExtra("first", countNumberChat.toString())
                        startActivity(intent)
                        finish()
                    }
                }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }*/

    override fun onStop() {
        super.onStop()
        mAuth!!.removeAuthStateListener(firebaseAuthStateListener!!)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            recreate()
            if (mLocationManager == null) {
                mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1) {
            if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recreate()
            } else {
                val intent = Intent(this@First_Activity, ShowGpsOpen::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.putExtra("2", "2")
                finish()
                startActivity(intent)
            }
        }
    }


    private inner class SimpleAnimationListener(private val context: Context?) : AnimationListener {
        override fun onAnimationEnd(hTextView: HTextView?) {
            hTextView!!.animateText("Welcome to my world")
        }

    }
}