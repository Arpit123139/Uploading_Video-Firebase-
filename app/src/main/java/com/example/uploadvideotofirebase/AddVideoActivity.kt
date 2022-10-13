package com.example.uploadvideotofirebase

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.widget.MediaController
import android.widget.ProgressBar
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_video.*
import kotlinx.android.synthetic.main.activity_videos.*

class AddVideoActivity : AppCompatActivity() {


    //constant for camera Permission
    private var VIDEO_PICK_GALLERY_CODE=100;
    private var VIDEO_PICK_CAMERA_CODE=101
    //constant to request Camera Permisiion
    private val CAMERA_REQUEST_CODE=102;

    // Array for Camera Request Permission
    private lateinit var cameraPermission:Array<String>

    private var videoUri: Uri?=null         // uri for the picked video

    private var title:String=""
    //ProgressDialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_video)

        supportActionBar?.hide()

        //init for camera Permission Array
        // We require two permissions so we are using the array
        cameraPermission= arrayOf(android.Manifest.permission.CAMERA,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        //init progressbar
        progressDialog= ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setMessage("Uploading Video ...")
        progressDialog.setCanceledOnTouchOutside(false)

        //Handle click upload Video
        uploadVideoBtn.setOnClickListener{

            //get title
            title=titleEt.text.toString();
            if(TextUtils.isEmpty(title)){
                Toast.makeText(this,"Title is Required ",Toast.LENGTH_SHORT).show();
            }else if(videoUri==null){
                // video is not picked
                Toast.makeText(this,"First Select the Video to upload ",Toast.LENGTH_SHORT).show();
            }else{
                //title entered.Video Picked
                uploadVideoFirebase()
            }

            val intent=Intent(this,VideosActivity::class.java);
            startActivity(intent);
            finish()
        }

        pickVideoFab.setOnClickListener{
            videoPickDialog()
        }

    }

    private fun uploadVideoFirebase() {
        //show  Progress
        progressDialog.show()

        //timeStamp
        val timeStamp=""+System.currentTimeMillis()

        //file path and name in firebase Storage
        val filePathAndName="Videos/video_$timeStamp"

        //STORAGE REFERENCE
        val storageReference=FirebaseStorage.getInstance().getReference(filePathAndName)
        //upload video using uri of video to storage

        storageReference.putFile(videoUri!!)
            .addOnSuccessListener { taskSnapshot ->
                // uploaded,get url of the uploaded video
                val uriTask=taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val downloadUri=uriTask.result

                if(uriTask.isSuccessful){
                    //video url is recieved successfully

                    //adding videoDetails to the firebase
                    val hashMap=HashMap<String,Any>()
                    hashMap["id"]="$timeStamp"
                    hashMap["title"]=title
                    hashMap["timestamp"]="$timeStamp"
                    hashMap["videoUri"]="$downloadUri"

                    //put the above info to databse
                    val dbRef=FirebaseDatabase.getInstance().getReference("Videos")
                    dbRef.child(timeStamp)
                        .setValue(hashMap)
                        .addOnSuccessListener { taskSnapshot->
                               //video Info added Successfully
                            progressDialog.dismiss();
                            Toast.makeText(this,"Video Uploaded",Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener{e->
                            Toast.makeText(this,"$e.message",Toast.LENGTH_SHORT).show()

                        }
                }
            }
            .addOnFailureListener{ e->
                //failed uploading
                progressDialog.dismiss();
                Toast.makeText(this,"${e.message}",Toast.LENGTH_SHORT).show()
            }

    }


    private fun setVideoToVideoView() {
        //Video Play Controlls
        val mediaController=MediaController(this)
        mediaController.setAnchorView(videoView)

        //set media controller
        videoView.setMediaController(mediaController)
        //set video uri
        videoView.setVideoURI(videoUri)
        videoView.requestFocus()
        videoView.setOnPreparedListener{
            //when video is ready by default dint play automatically
            videoView.pause()
        }
    }

    // Creating a DIALOG bG
    private fun videoPickDialog() {
        //options to display in dialog
        val options= arrayOf("Camera","Gallery")

        //alert Dialog
        val builder=AlertDialog.Builder(this)
        builder.setTitle("Pick Video From")
            .setItems(options){dialogInterface, i->
                //handle item clicks
                if(i==0){
                    //camera Clicked
                    if(!checkCameraPermissions()){
                        //PERMISSION WAS NOT ALLOWED,REQUEST
                        requestCameraPermissions()
                    }else{
                        videoPickCamera();
                    }
                }else{

                    //gallery clicked
                    videoPickGallery()
                }
            }.show()


    }

    private fun requestCameraPermissions(){
        //request Camera Permission
        ActivityCompat.requestPermissions(
            this,
            cameraPermission,
            CAMERA_REQUEST_CODE
        )
    }

    private fun checkCameraPermissions():Boolean{

        // check if camera permisiion that is camera and storage are allowed
        val result1= ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.CAMERA,
        )==PackageManager.PERMISSION_GRANTED

        val result2=ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )==PackageManager.PERMISSION_GRANTED
        //return true or false
        return result1 && result2
    }

    private fun videoPickCamera(){
        //video pick intent Camera
        val intent=Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        startActivityForResult(intent,VIDEO_PICK_CAMERA_CODE);            // After this it goes to onRequestPermissionResult
    }

    private fun videoPickGallery(){
        //video Pick Gallery
        val intent=Intent();
        intent.type="video/*"
        intent.action=Intent.ACTION_GET_CONTENT

        startActivityForResult(
            Intent.createChooser(intent,"Choose Video"),
            VIDEO_PICK_CAMERA_CODE
        )
    }



    override fun onNavigateUp(): Boolean {
        val intent=Intent(this,VideosActivity::class.java)                  // go to Previous Activity
        startActivity(intent);
        return super.onSupportNavigateUp();
    }

    // handles Permission Results
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        when(requestCode){
            CAMERA_REQUEST_CODE->
                if(grantResults.size>0){
                    //check if permission allowed or denied
                    val cameraAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED
                    val storageAccepted=grantResults[1]==PackageManager.PERMISSION_GRANTED
                    if(cameraAccepted && storageAccepted){
                        videoPickCamera()
                        //both permission allowed
                    }else{
                        //both or one of those are denied
                        Toast.makeText(this,"Permission denied," ,Toast.LENGTH_SHORT).show()
                    }

                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    //Handle video Pick Results
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(resultCode== RESULT_OK){
            //Videp is picked from camera or Gallery
            if(requestCode==VIDEO_PICK_CAMERA_CODE){
                videoUri=data!!.data
                setVideoToVideoView()
            }else if(requestCode==VIDEO_PICK_GALLERY_CODE){
                videoUri= data!!.data
                setVideoToVideoView()
            }
        }else
        {
            Toast.makeText(this,"Cancelled," ,Toast.LENGTH_SHORT).show()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}