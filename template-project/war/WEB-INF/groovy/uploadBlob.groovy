def blobs = blobstore.getUploadedBlobs(request)
def blobKey = blobs["myTextFile"]

response.status = 302

if (blobKey) {
	redirect "/success?key=${blobKey.keyString}"	
} else {
	redirect "/failure"
}
