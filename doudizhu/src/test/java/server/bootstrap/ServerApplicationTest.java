package server.bootstrap;

import org.junit.jupiter.api.Test;
import server.auth.SocketAuthenticator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerApplicationTest {

    @Test
    void should_authenticate_multiple_connections_in_parallel() throws Exception {
        ServerApplication application = new ServerApplication();
        BlockingAuthenticator authenticator = new BlockingAuthenticator();
        replaceAuthenticator(application, authenticator);

        Method acceptPlayers = ServerApplication.class.getDeclaredMethod("acceptPlayers", ServerSocket.class, int.class);
        acceptPlayers.setAccessible(true);

        Throwable[] failure = new Throwable[1];

        try (ServerSocket serverSocket = new ServerSocket(0);
             Socket firstClient = new Socket("127.0.0.1", serverSocket.getLocalPort());
             Socket secondClient = new Socket("127.0.0.1", serverSocket.getLocalPort())) {

            Thread acceptThread = new Thread(() -> {
                try {
                    acceptPlayers.invoke(application, serverSocket, 2);
                } catch (Throwable throwable) {
                    failure[0] = throwable;
                }
            });
            acceptThread.start();

            assertTrue(
                    authenticator.awaitBothAuthenticationsStarted(300),
                    "第二个连接应该在第一个登录完成前就进入认证"
            );

            authenticator.releaseFirstAuthentication();
            acceptThread.join(2_000);

            assertFalse(acceptThread.isAlive(), "收集到足够玩家后应该结束接入");
            assertEquals(List.of("fast", "slow"), readPlayerNames(application));
            assertEquals(null, failure[0]);
        }
    }

    @Test
    void should_not_count_authenticated_player_after_disconnect_before_room_is_full() throws Exception {
        ServerApplication application = new ServerApplication();
        replaceAuthenticator(application, new SequentialAuthenticator());

        Method acceptPlayers = ServerApplication.class.getDeclaredMethod("acceptPlayers", ServerSocket.class, int.class);
        acceptPlayers.setAccessible(true);

        Throwable[] failure = new Throwable[1];

        try (ServerSocket serverSocket = new ServerSocket(0)) {
            Thread acceptThread = new Thread(() -> {
                try {
                    acceptPlayers.invoke(application, serverSocket, 2);
                } catch (Throwable throwable) {
                    failure[0] = throwable;
                }
            });
            acceptThread.start();

            Socket firstClient = new Socket("127.0.0.1", serverSocket.getLocalPort());
            assertTrue(waitForPlayerNames(application, List.of("first")), "第一个玩家应该先认证成功");
            firstClient.close();
            assertTrue(waitForPlayerNames(application, List.of()), "断开的候场玩家应该被移除");

            try (Socket secondClient = new Socket("127.0.0.1", serverSocket.getLocalPort());
                 Socket thirdClient = new Socket("127.0.0.1", serverSocket.getLocalPort())) {
                acceptThread.join(2_000);

                assertFalse(acceptThread.isAlive(), "两个仍在线的玩家凑齐后才应该结束接入");
                assertEquals(List.of("second", "third"), readPlayerNames(application));
                assertEquals(null, failure[0]);
            }
        }
    }

    private void replaceAuthenticator(ServerApplication application, SocketAuthenticator authenticator) throws Exception {
        Field field = ServerApplication.class.getDeclaredField("authenticator");
        field.setAccessible(true);
        field.set(application, authenticator);
    }

    @SuppressWarnings("unchecked")
    private List<String> readPlayerNames(ServerApplication application) throws Exception {
        Field field = ServerApplication.class.getDeclaredField("registry");
        field.setAccessible(true);
        Object registry = field.get(application);
        Method method = registry.getClass().getDeclaredMethod("collectPlayerNames");
        return (List<String>) method.invoke(registry);
    }

    private boolean waitForPlayerNames(ServerApplication application, List<String> expectedNames) throws Exception {
        long deadline = System.currentTimeMillis() + 1_000;
        while (System.currentTimeMillis() < deadline) {
            if (expectedNames.equals(readPlayerNames(application))) {
                return true;
            }
            Thread.sleep(20);
        }
        return false;
    }

    private static final class BlockingAuthenticator extends SocketAuthenticator {
        private final CountDownLatch authenticationStarted = new CountDownLatch(2);
        private final CountDownLatch releaseFirst = new CountDownLatch(1);
        private final AtomicInteger authenticationOrder = new AtomicInteger();

        private BlockingAuthenticator() {
            super(null);
        }

        @Override
        public String authenticate(BufferedReader reader, PrintWriter writer) {
            int currentOrder = authenticationOrder.incrementAndGet();
            authenticationStarted.countDown();
            if (currentOrder == 1) {
                awaitRelease();
                return "slow";
            }
            return "fast";
        }

        private boolean awaitBothAuthenticationsStarted(long timeoutMillis) throws InterruptedException {
            return authenticationStarted.await(timeoutMillis, TimeUnit.MILLISECONDS);
        }

        private void releaseFirstAuthentication() {
            releaseFirst.countDown();
        }

        private void awaitRelease() {
            try {
                releaseFirst.await(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
    }

    private static final class SequentialAuthenticator extends SocketAuthenticator {
        private final AtomicInteger nextName = new AtomicInteger();

        private SequentialAuthenticator() {
            super(null);
        }

        @Override
        public String authenticate(BufferedReader reader, PrintWriter writer) {
            return switch (nextName.incrementAndGet()) {
                case 1 -> "first";
                case 2 -> "second";
                case 3 -> "third";
                default -> "extra";
            };
        }
    }
}
