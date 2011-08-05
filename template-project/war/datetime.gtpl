<% include '/WEB-INF/includes/header.gtpl' %>

<h1>$i18n.dateTime</h1>

<p>
    <%
        log.info "outputing the datetime attribute"
    %>
    $i18n.dateTime: <%= request.getAttribute('datetime') %>
</p>

<% include '/WEB-INF/includes/footer.gtpl' %>
