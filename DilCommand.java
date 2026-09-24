package ir.dilcraft;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;

public final class DilCommand implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;
    private final LinabaseClient db;

    public DilCommand(JavaPlugin plugin, LinabaseClient db){this.plugin=plugin;this.db=db;}

    public boolean onCommand(CommandSender s,Command c,String label,String[] a){
        if(!(s instanceof Player p)){s.sendMessage("Players only.");return true;}

        if(a.length==0){
            p.sendMessage(ChatColor.GRAY+"Checking Dil...");
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin,()->{
                try{
                    long v=db.getBalance(p.getName());
                    plugin.getServer().getScheduler().runTask(plugin,
                        ()->p.sendMessage(ChatColor.AQUA+"Dil: "+ChatColor.WHITE+v));
                }catch(Exception e){
                    plugin.getLogger().warning(e.getMessage());
                    plugin.getServer().getScheduler().runTask(plugin,
                        ()->p.sendMessage(ChatColor.RED+"Could not read Dil."));
                }
            });
            return true;
        }

        if(!p.hasPermission("dilcraft.admin")||a.length!=3){
            p.sendMessage(ChatColor.YELLOW+"/dil");
            p.sendMessage(ChatColor.YELLOW+"/dil set <player> <amount>");
            p.sendMessage(ChatColor.YELLOW+"/dil add <player> <amount>");
            p.sendMessage(ChatColor.YELLOW+"/dil remove <player> <amount>");
            return true;
        }

        String action=a[0].toLowerCase(), target=a[1];
        long amount;
        try{amount=Long.parseLong(a[2]);}
        catch(NumberFormatException e){p.sendMessage(ChatColor.RED+"Amount must be a number.");return true;}

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin,()->{
            try{
                long current=db.getBalance(target);
                long next=switch(action){
                    case "set"->amount;
                    case "add"->current+amount;
                    case "remove"->current-amount;
                    default->throw new IllegalArgumentException("Unknown action");
                };
                if(next<0)throw new IllegalArgumentException("Balance cannot be negative");
                db.setBalance(target,next);
                plugin.getServer().getScheduler().runTask(plugin,
                    ()->p.sendMessage(ChatColor.GREEN+target+"'s Dil: "+next));
            }catch(Exception e){
                plugin.getLogger().warning(e.getMessage());
                plugin.getServer().getScheduler().runTask(plugin,
                    ()->p.sendMessage(ChatColor.RED+"Failed: "+e.getMessage()));
            }
        });
        return true;
    }

    public List<String> onTabComplete(CommandSender s,Command c,String a,String[] args){
        if(args.length==1)return Arrays.asList("set","add","remove");
        return Collections.emptyList();
    }
}
