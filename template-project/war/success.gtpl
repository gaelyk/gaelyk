<% import com.google.appengine.api.blobstore.BlobKey %>
<html>
    <body>
        <h1>$i18n.successHeader</h1>
        <% def blob = new BlobKey(params.key) %>

        <div>
            $i18n.fileName: ${blob.filename} <br/>
            $i18n.contentType: ${blob.contentType}<br/>
            $i18n.creationDate: ${blob.creation}<br/>
            $i18n.size: ${blob.size}
        </div>

        <h2>$i18n.content</h2>
        
        <div>
            <% blob.withReader { out << it.text } %>
        </div>
    </body>
</html>

