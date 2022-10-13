package com.example.uploadvideotofirebase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_videos.*

/*This Activity Will Display The List Of Videos*/
class VideosActivity : AppCompatActivity() {

    // arrayList for videoList
    private lateinit var videoArrayList: ArrayList<ModelVideo>
    private lateinit var recyclerView: RecyclerView
    //adapter
    private lateinit var adapterVideo: AdapterVideo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_videos)

        //actionbar Title
        title="Videos"

        //init RecycleView
        recyclerView=findViewById(R.id.videosRv)
        recyclerView.layoutManager=LinearLayoutManager(this)

        //init adapter and ArrayList
        videoArrayList= ArrayList();
        adapterVideo=AdapterVideo(this,videoArrayList)
        recyclerView.adapter=adapterVideo

        loadVideosFromFirebase();

        supportActionBar?.hide()

        addVideoFab.setOnClickListener{
            startActivity(Intent(this,AddVideoActivity::class.java));
        }
    }

    private fun loadVideosFromFirebase() {

        //dbr ef
        val ref=FirebaseDatabase.getInstance().getReference("Videos")

        ref.addValueEventListener(object:ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before adding data
                videoArrayList.clear()
                for(ds in snapshot.children){
                    //get data as Model
                    val modelVideo=ds.getValue(ModelVideo::class.java)
                    //add array to ArrayList
                    videoArrayList.add(modelVideo!!);
                }
                adapterVideo.notifyDataSetChanged()


            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@VideosActivity,"Error in loading the video",Toast.LENGTH_SHORT).show();
            }


        })
    }
}