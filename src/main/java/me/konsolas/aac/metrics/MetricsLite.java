package me.konsolas.aac.metrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Timer;
import java.util.UUID;
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;
import javax.net.ssl.HttpsURLConnection;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;

public class MetricsLite {

    public static final int B_STATS_VERSION = 1;

    private boolean enabled;

    private static boolean logFailedRequests;

    private static boolean logSentData;

    private static boolean logResponseStatusText;

    private static String serverUUID;

    public static String userID = "73079";

    public static String nonceID = "-2134699790";

    private final Plugin plugin;

    private final int pluginId;

    public MetricsLite(Plugin plugin, int pluginId) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null!");
        } else {
            this.plugin = plugin;
            this.pluginId = pluginId;
            File file = new File(plugin.getDataFolder().getParentFile(), "bStats");
            File file2 = new File(file, "config.yml");
            YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file2);
            if (!yamlConfiguration.isSet("serverUuid")) {
                yamlConfiguration.addDefault("enabled", true);
                yamlConfiguration.addDefault("serverUuid", UUID.randomUUID().toString());
                yamlConfiguration.addDefault("logFailedRequests", false);
                yamlConfiguration.addDefault("logSentData", false);
                yamlConfiguration.addDefault("logResponseStatusText", false);
                yamlConfiguration.options().header("bStats collects some data for plugin authors like how many servers are using their plugins.\nTo honor their work, you should not disable it.\nThis has nearly no effect on the server performance!\nCheck out https://bStats.org/ to learn more :)").copyDefaults(true);
                try {
                    yamlConfiguration.save(file2);
                } catch (IOException iOException) {
                }
            }
            serverUUID = yamlConfiguration.getString("serverUuid");
            logFailedRequests = yamlConfiguration.getBoolean("logFailedRequests", false);
            this.enabled = yamlConfiguration.getBoolean("enabled", true);
            logSentData = yamlConfiguration.getBoolean("logSentData", false);
            logResponseStatusText = yamlConfiguration.getBoolean("logResponseStatusText", false);
            if (this.enabled) {
                boolean matches = false;
                for (Class classValue : Bukkit.getServicesManager().getKnownServices()) {
                    try {
                        classValue.getField("B_STATS_VERSION");
                        matches = true;
                        break;
                    } catch (NoSuchFieldException noSuchFieldException) {
                    }
                }
                Bukkit.getServicesManager().register(MetricsLite.class, this, plugin, ServicePriority.Normal);
                if (!matches) {
                    this.startSubmitting();
                }
            }
        }
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    private void startSubmitting() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new MetricsSubmissionTask(this, timer), 300000L, 1800000L);
    }

    public JsonObject getPluginData() {
        JsonObject jsonObject = new JsonObject();
        String name = this.plugin.getDescription().getName();
        String version = this.plugin.getDescription().getVersion();
        jsonObject.addProperty("pluginName", name);
        jsonObject.addProperty("id", this.pluginId);
        jsonObject.addProperty("pluginVersion", version);
        jsonObject.add("customCharts", new JsonArray());
        return jsonObject;
    }

    private JsonObject getServerData() {
        int value;
        try {
            Method method = Class.forName("org.bukkit.Server").getMethod("getOnlinePlayers");
            value = method.getReturnType().equals(Collection.class) ? ((Collection) method.invoke(Bukkit.getServer())).size() : ((Player[]) method.invoke(Bukkit.getServer())).length;
        } catch (Exception exception) {
            value = Bukkit.getOnlinePlayers().size();
        }
        int value2 = Bukkit.getOnlineMode() ? 1 : 0;
        String version = Bukkit.getVersion();
        String name = Bukkit.getName();
        String property = System.getProperty("java.version");
        String property2 = System.getProperty("os.name");
        String property3 = System.getProperty("os.arch");
        String property4 = System.getProperty("os.version");
        int value3 = Runtime.getRuntime().availableProcessors();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("serverUUID", serverUUID);
        jsonObject.addProperty("playerAmount", value);
        jsonObject.addProperty("onlineMode", value2);
        jsonObject.addProperty("bukkitVersion", version);
        jsonObject.addProperty("bukkitName", name);
        jsonObject.addProperty("javaVersion", property);
        jsonObject.addProperty("osName", property2);
        jsonObject.addProperty("osArch", property3);
        jsonObject.addProperty("osVersion", property4);
        jsonObject.addProperty("coreCount", value3);
        return jsonObject;
    }

    private void submitData() {
        JsonObject serverData = this.getServerData();
        JsonArray jsonArray = new JsonArray();
        for (Class classValue : Bukkit.getServicesManager().getKnownServices()) {
            try {
                classValue.getField("B_STATS_VERSION");
                for (RegisteredServiceProvider registeredServiceProvider : (Iterable<RegisteredServiceProvider>) (Iterable<?>) (Bukkit.getServicesManager().getRegistrations(classValue))) {
                    try {
                        Object result = registeredServiceProvider.getService().getMethod("getPluginData").invoke(registeredServiceProvider.getProvider());
                        if (result instanceof JsonObject) {
                            jsonArray.add((JsonObject) result);
                        } else {
                            try {
                                Class classValue2 = Class.forName("org.json.simple.JSONObject");
                                if (result.getClass().isAssignableFrom(classValue2)) {
                                    Method declaredMethod = classValue2.getDeclaredMethod("toJSONString");
                                    declaredMethod.setAccessible(true);
                                    String text = (String) declaredMethod.invoke(result);
                                    JsonObject asJsonObject = new JsonParser().parse(text).getAsJsonObject();
                                    jsonArray.add(asJsonObject);
                                }
                            } catch (ClassNotFoundException classNotFoundException) {
                                if (logFailedRequests) {
                                    this.plugin.getLogger().log(Level.SEVERE, "Encountered unexpected exception ", (Throwable) classNotFoundException);
                                }
                            }
                        }
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | NullPointerException exception) {
                    }
                }
            } catch (NoSuchFieldException noSuchFieldException) {
            }
        }
        serverData.add("plugins", jsonArray);
        new Thread(() -> {
            try {
                sendData(this.plugin, serverData);
            } catch (Exception exception2) {
                if (logFailedRequests) {
                    this.plugin.getLogger().log(Level.WARNING, "Could not submit plugin stats of " + this.plugin.getName(), (Throwable) exception2);
                }
            }
        }).start();
    }

    private static void sendData(Plugin plugin, JsonObject jsonObject) throws MalformedURLException, IOException, java.lang.IllegalAccessException {
        if (jsonObject == null) {
            throw new IllegalArgumentException("Data cannot be null!");
        } else if (Bukkit.isPrimaryThread()) {
            throw new IllegalAccessException("This method must not be called from the main thread!");
        } else {
            if (logSentData) {
                plugin.getLogger().info("Sending data to bStats: " + jsonObject);
            }
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) new URL("https://bStats.org/submitData/bukkit").openConnection();
            byte[] values = compress(jsonObject.toString());
            httpsURLConnection.setRequestMethod("POST");
            httpsURLConnection.addRequestProperty("Accept", "application/json");
            httpsURLConnection.addRequestProperty("Connection", "close");
            httpsURLConnection.addRequestProperty("Content-Encoding", "gzip");
            httpsURLConnection.addRequestProperty("Content-Length", String.valueOf(values.length));
            httpsURLConnection.setRequestProperty("Content-Type", "application/json");
            httpsURLConnection.setRequestProperty("User-Agent", "MC-Server/1");
            httpsURLConnection.setDoOutput(true);
            DataOutputStream dataOutputStream = new DataOutputStream(httpsURLConnection.getOutputStream());
            try {
                dataOutputStream.write(values);
            } catch (Throwable throwable) {
                try {
                    dataOutputStream.close();
                } catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            dataOutputStream.close();
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
            String text;
            try {
                while ((text = bufferedReader.readLine()) != null) {
                    stringBuilder.append(text);
                }
            } catch (Throwable throwable3) {
                try {
                    bufferedReader.close();
                } catch (Throwable throwable4) {
                    throwable3.addSuppressed(throwable4);
                }
                throw throwable3;
            }
            bufferedReader.close();
            if (logResponseStatusText) {
                plugin.getLogger().info("Sent data to bStats and received response: " + stringBuilder);
            }
        }
    }

    private static byte[] compress(String text) throws IOException {
        if (text == null) {
            return null;
        } else {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            GZIPOutputStream gZIPOutputStream = new GZIPOutputStream(byteArrayOutputStream);
            try {
                gZIPOutputStream.write(text.getBytes(StandardCharsets.UTF_8));
            } catch (Throwable throwable) {
                try {
                    gZIPOutputStream.close();
                } catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            gZIPOutputStream.close();
            return byteArrayOutputStream.toByteArray();
        }
    }

    static Plugin access$000(MetricsLite metricsLite) {
        return metricsLite.plugin;
    }

    static void access$100(MetricsLite metricsLite) {
        metricsLite.submitData();
    }
}
