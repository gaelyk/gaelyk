<html>
    <body>
        <h1>$i18n.uploadHeader</h1>
        <form action="${blobstore.createUploadUrl('/uploadBlob.groovy')}" 
                method="post" enctype="multipart/form-data">
            <input type="file" name="myTextFile">
            <input type="submit" value="Submit">
        </form>
    </body>
</html>
