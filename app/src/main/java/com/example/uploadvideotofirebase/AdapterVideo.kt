package com.example.uploadvideotofirebase

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text
import java.util.*
import kotlin.collections.ArrayList

class AdapterVideo(private var context: Context,private var arr:ArrayList<ModelVideo>):RecyclerView.Adapter<AdapterVideo.HolderView>() {

    class HolderView(itemView: View):RecyclerView.ViewHolder(itemView){

        var videoView: VideoView =itemView.findViewById(R.id.videoView)
        var titleTv: TextView =itemView.findViewById(R.id.titleTv)
        var timeTv:TextView=itemView.findViewById(R.id.timeTv);
        var progressBar:ProgressBar=itemView.findViewById(R.id.progressBar)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderView {

        // inflate the videos
        val view=LayoutInflater.from(context).inflate(R.layout.row_video,parent,false)
        return HolderView(view);
    }

    override fun onBindViewHolder(holder: HolderView, position: Int) {

        val modelVideo=arr[position]

        //get Specific data
        val id:String?=modelVideo.id
        val title:String?=modelVideo.title
        val timestamp:String?=modelVideo.timestamp
        val videoUri:String?=modelVideo.videoUri

        // format date e.g 28/09/2020
        val calendar=Calendar.getInstance()
        calendar.timeInMillis=timestamp!!.toLong()
        val formattedDateTime=android.text.format.DateFormat.format("dd/MM/yyyy K:mm",calendar).toString()

        //setData
        holder.titleTv.text=title
        holder.timeTv.text="Uploaded at : $formattedDateTime"


        setVideoUrl(modelVideo,holder)

    }

    private fun setVideoUrl(modelVideo: ModelVideo ,holder:HolderView) {

        //show Progress
        holder.progressBar.visibility=View.VISIBLE;
        //get Video Url
        val vedioUrl=modelVideo.videoUri                     // In the database we are setting the url not the uri

        //set Media Controller for play/pause
        val mediaController=MediaController(context)
        mediaController.setAnchorView(holder.videoView)
        val vedioUri= Uri.parse(vedioUrl)                      // we are getting back the uri from the stores url

        holder.videoView.setMediaController(mediaController)
        holder.videoView.setVideoURI(vedioUri)
        holder.videoView.requestFocus();                 // try to focus on specific view

        holder.videoView.setOnPreparedListener { mediaPlayer->
        // video is prepared to play
            mediaPlayer.start()
        }

        holder.videoView.setOnInfoListener (MediaPlayer.OnInfoListener{mp,what,extra->
            //check if buffering or rendering

            when(what){
                MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START->{
                    //rendering Started
                    holder.progressBar.visibility=View.VISIBLE
                    return@OnInfoListener true
                }
                MediaPlayer.MEDIA_INFO_BUFFERING_START->{
                    //buffering Started
                    holder.progressBar.visibility=View.VISIBLE
                    return@OnInfoListener true
                }
                MediaPlayer.MEDIA_INFO_BUFFERING_END->{
                    //buffering Started
                    holder.progressBar.visibility=View.GONE
                    return@OnInfoListener true
                }
            }
            false
        })

        holder.videoView.setOnCompletionListener { mediaPlayer->
            //restart vedio when completed \ loop video
            mediaPlayer.start()
        }


    }

    override fun getItemCount(): Int {
        return arr.size;
    }
}