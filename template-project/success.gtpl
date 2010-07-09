<% import com.google.appengine.api.blobstore.BlobKey %>
<html>
    <body>
        <h1>Success</h1>
        <% def blob = new BlobKey(params.key) %>

        <div>
            File name: ${blob.filename} <br/>
            Content type: ${blob.contentType}<br/>
            Creation date: ${blob.creation}<br/>
            Size: ${blob.size}
        </div>

        <h2>Content of the blob</h2>
        
        <div>
            <% blob.withReader { out << it.text } %>
        </div>
    </body>
</html>

