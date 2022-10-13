package com.example.uploadvideotofirebase

class ModelVideo {

    //variables use same name as in firebase
    var id:String?=null
    var title:String?=null
    var timestamp:String?=null
    var videoUri:String?=null

    constructor(){
        //this constructor is required by the firebase
    }

    constructor(id: String?, title: String?, timestamp: String?, videoUri: String?) {
        this.id = id
        this.title = title
        this.timestamp = timestamp
        this.videoUri = videoUri
    }


}