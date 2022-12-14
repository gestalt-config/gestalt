package org.github.gestalt.config.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.exceptions.GestaltException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class GuiceTest {

    private Injector injector;

    private Gestalt mockGestalt;

    @BeforeEach
    public void setup() {
        mockGestalt = Mockito.mock(Gestalt.class);
        injector = Guice.createInjector(new GestaltModule(mockGestalt));
    }

    @Test
    public void testGuiceField() throws GestaltException {
        DBConnection dbUser = new DBConnection("mySQL", 1234, "admin", "abcdef");
        Mockito.when(mockGestalt.getConfig("db.user", DBConnection.class)).thenReturn(dbUser);

        MyService service = injector.getInstance(MyService.class);

        Assertions.assertEquals(dbUser, service.getConnection());
    }

    @Test
    public void testGuiceSimpleConfig() throws GestaltException {

        Mockito.when(mockGestalt.getConfig("db.uri", String.class)).thenReturn("myhost");
        Mockito.when(mockGestalt.getConfig("db.port", Integer.class)).thenReturn(1234);
        Mockito.when(mockGestalt.getConfig("db.username", String.class)).thenReturn("admin");
        Mockito.when(mockGestalt.getConfig("db.password", String.class)).thenReturn("password");

        MyServiceSimple service = injector.getInstance(MyServiceSimple.class);

        Assertions.assertEquals("myhost", service.uri);
        Assertions.assertEquals(1234, service.port);
        Assertions.assertEquals("admin", service.username);
        Assertions.assertEquals("password", service.password);
    }

    @Test
    public void testGuiceException() throws GestaltException {

        Mockito.when(mockGestalt.getConfig("db.user", DBConnection.class)).thenThrow(new GestaltException("injection Error"));

        ProvisionException e = Assertions.assertThrows(ProvisionException.class, () -> injector.getInstance(MyService.class));

        Assertions.assertEquals("injection Error", e.getCause().getCause().getMessage());
    }

    public static class MyService {
        private @InjectConfig(path = "db.user") DBConnection connection;

        public DBConnection getConnection() {
            return connection;
        }
    }

    public static class MyServiceSimple {

        @InjectConfig(path = "db.uri")
        public String uri;

        @InjectConfig(path = "db.port")
        public Integer port;

        @InjectConfig(path = "db.username")
        public String username;

        @InjectConfig(path = "db.password")
        public String password;
    }


    public static class DBConnection {
        public String uri;
        public Integer port;
        public String username;
        public String password;

        public DBConnection() {

        }

        public DBConnection(String uri, Integer port, String username, String password) {
            this.uri = uri;
            this.port = port;
            this.username = username;
            this.password = password;
        }
    }
}
