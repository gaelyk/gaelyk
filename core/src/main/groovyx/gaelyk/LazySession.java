package groovyx.gaelyk;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

/**
 * Lazy session is initialized from the parent request
 * whenever any method is called.
 * 
 * @author Vladimir Orany
 *
 */
@SuppressWarnings("deprecation") public class LazySession implements HttpSession {

    private HttpSession session;
    private final HttpServletRequest request;
    
    public LazySession(HttpServletRequest request) {
        this.request = request;
    }

    @Override public long getCreationTime() {
        return getSession().getCreationTime();
    }

    @Override public String getId() {
        return getSession().getId();
    }

    @Override public long getLastAccessedTime() {
        return getSession().getLastAccessedTime();
    }

    @Override public ServletContext getServletContext() {
        return getSession().getServletContext();
    }

    @Override public void setMaxInactiveInterval(int interval) {
        getSession().setMaxInactiveInterval(interval);
    }

    @Override public int getMaxInactiveInterval() {
        return getSession().getMaxInactiveInterval();
    }

    @Override public HttpSessionContext getSessionContext() {
        return getSession().getSessionContext();
    }

    @Override public Object getAttribute(String name) {
        return getSession().getAttribute(name);
    }

    @Override public Object getValue(String name) {
        return getSession().getValue(name);
    }

    @Override public Enumeration getAttributeNames() {
        return getSession().getAttributeNames();
    }

    @Override public String[] getValueNames() {
        return getSession().getValueNames();
    }

    @Override public void setAttribute(String name, Object value) {
        getSession().setAttribute(name, value);

    }

    @Override public void putValue(String name, Object value) {
        getSession().putValue(name, value);
    }

    @Override public void removeAttribute(String name) {
        getSession().removeAttribute(name);
    }

    @Override public void removeValue(String name) {
        getSession().removeValue(name);
    }

    @Override public void invalidate() {
        getSession().invalidate();
    }

    @Override public boolean isNew() {
        return getSession().isNew();
    }

    private HttpSession getSession() {
        if (session != null) return session;
        this.session = request.getSession(true);
        return session;
    }
}
