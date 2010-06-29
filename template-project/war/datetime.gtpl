<% include '/WEB-INF/includes/header.gtpl' %>

<h1>Date / time</h1>

<p>
    <%
        log.info "outputing the datetime attribute"
    %>
    The current date and time: <%= request.getAttribute('datetime') %>
</p>

<% include '/WEB-INF/includes/footer.gtpl' %>

