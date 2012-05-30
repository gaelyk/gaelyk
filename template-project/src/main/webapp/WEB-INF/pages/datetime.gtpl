<% include '/WEB-INF/includes/header.gtpl' %>
<div class="hero-unit">
<h1>Current Date</h1>

<p>
    <%
        log.info "outputing the datetime attribute"
    %>
    The current date and time: <%= request.getAttribute('datetime') %>
</p>
</div>
<% include '/WEB-INF/includes/footer.gtpl' %>

