package com.example.mehmeterdin.artbookwithkotlin

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.view.View
import kotlinx.android.synthetic.main.activity_main2.*
import java.io.ByteArrayOutputStream

class Main2Activity : AppCompatActivity() {

    var selectedImage : Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        val intent = intent
        val info = intent.getStringArrayExtra("info")
        if (info.equals("new")){
            imageView.setImageBitmap(BitmapFactory.decodeResource(applicationContext.resources,R.drawable.search))
            etTitle.setText("")
        }else{
            etTitle.setText(intent.getStringExtra("name"))
            val chosen = Globals.Chosen
            imageView.setImageBitmap(chosen.returnImage())

            button.visibility = View.INVISIBLE
        }
    }
    @RequiresApi(Build.VERSION_CODES.M)
    fun chooseImage(view: View){
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),2)
        }else{
            val intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent,1)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 2){
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                val intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent,1)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null){
            val image = data.data
            try {
                selectedImage = MediaStore.Images.Media.getBitmap(this.contentResolver,image)
                imageView.setImageBitmap(selectedImage)
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
    fun save (view: View){
        val artName = etTitle.text.toString()

        val outputStream = ByteArrayOutputStream()
        selectedImage?.compress(Bitmap.CompressFormat.PNG,50,outputStream)
        val byteArray = outputStream.toByteArray()

        try {
            val database= this.openOrCreateDatabase("Arts", Context.MODE_PRIVATE,null)
            database.execSQL("CREATE TABLE IF NOT EXISTS arts (name VARCHAR,image BLOB)")
            val sqlString = "INSERT INTO arts VALUES (?, ?)"
            val statement = database.compileStatement(sqlString)
            statement.bindString(1,artName)
            statement.bindBlob(2,byteArray)
            statement.execute()
        }catch (e: Exception){
            e.printStackTrace()
        }
        val intent = Intent(applicationContext,MainActivity::class.java)
        startActivity(intent)

    }
}
