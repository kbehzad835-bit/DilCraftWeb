package ir.dilcraft;

import org.bukkit.plugin.java.JavaPlugin;

public final class DilCraftPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();
        String key = getConfig().getString("api-key", "");
        if (key.isBlank() || key.equals("PUT_YOUR_ANON_KEY_HERE")) {
            getLogger().severe("Set api-key in plugins/DilCraft/config.yml");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        LinabaseClient db = new LinabaseClient(
            getConfig().getString("api-url", "https://linabase.com"),
            key,
            getConfig().getString("table", "users"),
            getConfig().getString("username-column", "username"),
            getConfig().getString("balance-column", "dil")
        );
        DilCommand cmd = new DilCommand(this, db);
        getCommand("dil").setExecutor(cmd);
        getCommand("dil").setTabCompleter(cmd);
        getLogger().info("DilCraft enabled.");
    }
}
