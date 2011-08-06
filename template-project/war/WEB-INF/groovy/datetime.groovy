
log.info "Setting attribute datetime"

request.setAttribute 'datetime', new Date().toString()

log.info "Forwarding to the template"

forward '/WEB-INF/pages/datetime.gtpl'