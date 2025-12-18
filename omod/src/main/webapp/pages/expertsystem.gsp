<% ui.decorateWith("appui", "standardEmrPage") %>

<% if (context.authenticated) { %>
    $context.authenticatedUser.personName.fullName.
    <% context.authenticatedUser.roles.findAll { !it.retired }.each { %>
        $it.role ($it.description)
    <% } %>
<% } else { %>
    You are not logged in.
<% } %>
