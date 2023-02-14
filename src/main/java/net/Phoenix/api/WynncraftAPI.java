package net.Phoenix.api;

import net.Phoenix.utilities.ResourceRateLimit;
import net.Phoenix.utilities.Utilities;
import net.Phoenix.api.objects.Player;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class WynncraftAPI {

    public Player getPlayerStats(String player) throws IOException {
        String url = WynncraftEndpoints.PLAYER.getUrl().replace("{PLAYER}", player);
        return Player.deserialize(Utilities.queryAPI(url));
    }

    public static List<Player> getPlayersStats(List<String> playerNames) {
        ResourceRateLimit second = new ResourceRateLimit(50, Duration.ofSeconds(1));
        ResourceRateLimit minute = new ResourceRateLimit(180, Duration.ofMinutes(1));

        int numberOfRequests = playerNames.size();
        ExecutorService executor = Executors.newFixedThreadPool(numberOfRequests);
        List<Future<Player>> futures = new ArrayList<>();
        for (String playerName : playerNames) {
            PlayerRequest requestThread = new PlayerRequest(playerName, second, minute);
            Future<Player> future = executor.submit(requestThread);
            futures.add(future);
        }
        List<Player> results = new ArrayList<>();
        for (Future<Player> future : futures) {
            try {
                results.add(future.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();
        return results;
    }

    public enum WynncraftEndpoints {
        PLAYER("https://api.wynncraft.com/v2/player/{PLAYER}/stats");


        private final String url;

        WynncraftEndpoints(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }
    }
}
