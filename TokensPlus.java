package org.tokensplus.tokensplus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;

public class TokensPlus extends JavaPlugin implements CommandExecutor, Listener {

    private final Map<String, Integer> tokenBalance = new HashMap<>();

    @Override
    public void onEnable() {
        getCommand("tok").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("TokensPlus включен");
    }

    @Override
    public void onDisable() {
        getLogger().info("TokensPlus выключен");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("tok")) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Использование: /tok <команда> [аргументы]");
                return true;
            }

            String subCommand = args[0];
            if (subCommand.equalsIgnoreCase("give") && args.length == 3) {
                return handleGive(sender, args[1], args[2]);
            } else if (subCommand.equalsIgnoreCase("set") && args.length == 3) {
                return handleSet(sender, args[1], args[2]);
            } else if (subCommand.equalsIgnoreCase("mines") && args.length == 3) {
                return handleMines(sender, args[1], args[2]);
            } else if (subCommand.equalsIgnoreCase("see") && args.length == 2) {
                return handleSee(sender, args[1]);
            } else if (subCommand.equalsIgnoreCase("took") && args.length == 3) {
                return handleTook(sender, args[1], args[2]);
            } else if (subCommand.equalsIgnoreCase("add") && args.length == 3) {
                return handleAdd(sender, args[1], args[2]);
            } else if (subCommand.equalsIgnoreCase("help")) {
                return handleHelp(sender);
            } else if (subCommand.equalsIgnoreCase("gui")) {
                return handleGui((Player) sender);
            } else {
                sender.sendMessage(ChatColor.RED + "Неверная команда или аргументы.");
                return true;
            }
        }
        return false;
    }

    private boolean handleGive(CommandSender sender, String playerName, String amount) {
        if (!(sender instanceof ConsoleCommandSender) && !sender.hasPermission("tokensplus.admin")) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав для использования этой команды.");
            return true;
        }

        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Игрок не найден.");
            return true;
        }

        try {
            int tokens = Integer.parseInt(amount);
            if (tokens <= 0) {
                sender.sendMessage(ChatColor.RED + "Количество должно быть положительным.");
                return true;
            }

            tokenBalance.put(player.getName(), tokenBalance.getOrDefault(player.getName(), 0) + tokens);
            sender.sendMessage(ChatColor.GREEN + "Выдали " + tokens + " токенов игроку " + player.getName());
            player.sendMessage(ChatColor.GREEN + "Вы получили " + tokens + " токенов!");

        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Неверное количество.");
        }
        return true;
    }

    private boolean handleSet(CommandSender sender, String playerName, String amount) {
        if (!(sender instanceof ConsoleCommandSender) && !sender.hasPermission("tokensplus.admin")) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав для использования этой команды.");
            return true;
        }

        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Игрок не найден.");
            return true;
        }

        try {
            int tokens = Integer.parseInt(amount);
            if (tokens < 0) {
                sender.sendMessage(ChatColor.RED + "Количество не может быть отрицательным.");
                return true;
            }

            tokenBalance.put(player.getName(), tokens);
            sender.sendMessage(ChatColor.GREEN + "Установлено " + tokens + " токенов игроку " + player.getName());
            player.sendMessage(ChatColor.GREEN + "Ваши токены установлены на " + tokens);

        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Неверное количество.");
        }
        return true;
    }

    private boolean handleMines(CommandSender sender, String playerName, String amount) {
        return handleAdjustment(sender, playerName, amount, true);
    }

    private boolean handleTook(CommandSender sender, String playerName, String amount) {
        return handleAdjustment(sender, playerName, amount, false);
    }

    private boolean handleAdjustment(CommandSender sender, String playerName, String amount, boolean isMines) {
        if (!(sender instanceof ConsoleCommandSender) && !sender.hasPermission("tokensplus.admin")) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав для использования этой команды.");
            return true;
        }

        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Игрок не найден.");
            return true;
        }

        try {
            int tokens = Integer.parseInt(amount);
            if (tokens <= 0) {
                sender.sendMessage(ChatColor.RED + "Количество должно быть положительным.");
                return true;
            }

            int currentTokens = tokenBalance.getOrDefault(player.getName(), 0);
            if (isMines) {
                if (currentTokens < tokens) {
                    sender.sendMessage(ChatColor.RED + "Недостаточно токенов.");
                    return true;
                }
                tokenBalance.put(player.getName(), currentTokens - tokens);
                sender.sendMessage(ChatColor.GREEN + "Убрано " + tokens + " токенов у игрока " + player.getName());
                player.sendMessage(ChatColor.GREEN + "С вашего баланса" + tokens + " токенов было убрано.");
            } else {
                tokenBalance.put(player.getName(), currentTokens + tokens);
                sender.sendMessage(ChatColor.GREEN + "Добавлено " + tokens + " токенов игроку " + player.getName());
                player.sendMessage(ChatColor.GREEN + "Вы получили " + tokens + " токенов.");
            }

        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Неверное количество.");
        }
        return true;
    }

    private boolean handleAdd(CommandSender sender, String playerName, String amount) {
        if (!(sender instanceof ConsoleCommandSender) && !sender.hasPermission("tokensplus.admin")) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав для использования этой команды.");
            return true;
        }

        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Игрок не найден.");
            return true;
        }

        try {
            int tokens = Integer.parseInt(amount);
            if (tokens <= 0) {
                sender.sendMessage(ChatColor.RED + "Количество должно быть положительным.");
                return true;
            }

            tokenBalance.put(player.getName(), tokenBalance.getOrDefault(player.getName(), 0) + tokens);
            sender.sendMessage(ChatColor.GREEN + "Добавлено " + tokens + " токенов игроку " + player.getName());
            player.sendMessage(ChatColor.GREEN + "Вы получили " + tokens + " токенов!");

        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Неверное количество.");
        }
        return true;
    }

    private boolean handleSee(CommandSender sender, String playerName) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Эта команда может быть использована только игроками.");
            return true;
        }

        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Игрок не найден.");
            return true;
        }

        int tokens = tokenBalance.getOrDefault(player.getName(), 0);
        sender.sendMessage(ChatColor.GREEN + "У игрока " + player.getName() + " " + tokens + " токенов 💠");
        return true;
    }

    private boolean handleHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "--- Помощь Tokens+ ---");
        sender.sendMessage(ChatColor.GREEN + "/tok give <player> <amount>" + ChatColor.WHITE + " - Выдать токены игроку");
        sender.sendMessage(ChatColor.GREEN + "/tok set <player> <amount>" + ChatColor.WHITE + " - Установить токены игроку");
        sender.sendMessage(ChatColor.GREEN + "/tok mines <player> <amount>" + ChatColor.WHITE + " - Уменьшить токены у игрока");
        sender.sendMessage(ChatColor.GREEN + "/tok see <player>" + ChatColor.WHITE + " - Посмотреть количество токенов у игрока");
        sender.sendMessage(ChatColor.GREEN + "/tok took <player> <amount>" + ChatColor.WHITE + " - Забрать токены у игрока");
        sender.sendMessage(ChatColor.GREEN + "/tok add <player> <amount>" + ChatColor.WHITE + " - Добавить токены игроку");
        sender.sendMessage(ChatColor.GREEN + "/tok gui" + ChatColor.WHITE + " - Открыть GUI меню");
        sender.sendMessage(ChatColor.GREEN + "/tok help" + ChatColor.WHITE + " - Показать это сообщение");
        return true;
    }

    private boolean handleGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_PURPLE + "Tokens+ Menu");

        // Заполнение фиолетовыми стеклянными панелями
        ItemStack purplePane = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        ItemMeta purpleMeta = purplePane.getItemMeta();
        if (purpleMeta != null) {
            purpleMeta.setDisplayName(ChatColor.DARK_PURPLE + " ");
            purplePane.setItemMeta(purpleMeta);
        }

        for (int i = 0; i < 27; i++) {
            if (i == 13) continue; // Пропуск центрального слота
            gui.setItem(i, purplePane);
        }

        // Золото в центре
        ItemStack gold = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta goldMeta = gold.getItemMeta();
        if (goldMeta != null) {
            goldMeta.setDisplayName(ChatColor.GOLD + "Ваши токены: " + tokenBalance.getOrDefault(player.getName(), 0));
            gold.setItemMeta(goldMeta);
        }
        gui.setItem(13, gold);

        // Зеленая морская лампа
        ItemStack greenLantern = new ItemStack(Material.SEA_LANTERN);
        ItemMeta greenMeta = greenLantern.getItemMeta();
        if (greenMeta != null) {
            greenMeta.setDisplayName(ChatColor.GREEN + "Топ Баланс");
            greenLantern.setItemMeta(greenMeta);
        }
        gui.setItem(22, greenLantern);

        // TNT с рекламой
        ItemStack tnt = new ItemStack(Material.TNT);
        ItemMeta tntMeta = tnt.getItemMeta();
        if (tntMeta != null) {
            tntMeta.setDisplayName(ChatColor.RED + "Tokens+ создан VerMaxTj55");
            tntMeta.setLore(java.util.Arrays.asList(ChatColor.GRAY + "Discord: @vermax55", ChatColor.GRAY + "Поддержите донатом!"));
            tnt.setItemMeta(tntMeta);
        }
        gui.setItem(10, tnt);

        player.openInventory(gui);
        return true;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!tokenBalance.containsKey(event.getPlayer().getName())) {
            tokenBalance.put(event.getPlayer().getName(), 0);
        }
    }
}
