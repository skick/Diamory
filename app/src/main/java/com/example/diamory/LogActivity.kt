package com.example.diamory

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_log.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class LogActivity : AppCompatActivity() {


    var imageSet: Boolean = false           //used to check if log has image or not
    val dbHandler = SQLiteHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)

        showDate()
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
        diaryText.movementMethod = ScrollingMovementMethod()

        button_save.setOnClickListener {
            val logText = diaryText.text.toString()
            val currentTime = LocalDateTime.now().format(formatter).toString()
            if(imageView.drawable != null){
                val bitmap = (imageView.drawable as BitmapDrawable).bitmap
                dbHandler.addLogWPic(DiaryModel(currentTime, logText, Tools.getBytes(bitmap)))
            } else {
                Toast.makeText(this, "addlog without picture", Toast.LENGTH_LONG).show()
                dbHandler.addLog(DiaryModel(currentTime, logText))
            }
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            Toast.makeText(this, "Log saved.", Toast.LENGTH_LONG).show()

        }

        button_add_image.setOnClickListener{
            //checking runtime permission
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                    //permission is denied
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    requestPermissions(permissions, PERMISSION_CODE)
                } else {
                    //permission given
                    pickGalleryImage()
                }

            } else{
                //using lower than Android 10.0, no need for permission
                pickGalleryImage()
            }
        }
    }

    //show current date
    private fun showDate(){
        val current = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        val formattedDate = current.format(dateFormatter)
        val textView: TextView = findViewById(R.id.dateText) as TextView
        textView.setText(formattedDate)

    }

    //pick image from gallery
    private fun pickGalleryImage(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
        imageSet = true
    }

    companion object{
        //image pick code
        private val IMAGE_PICK_CODE = 1000
        //permission code
        private val PERMISSION_CODE = 1001
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            PERMISSION_CODE -> {
                if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //permission granted from popup
                    pickGalleryImage()
                } else{
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            imageView.setImageURI(data?.data)
        }
    }
}
