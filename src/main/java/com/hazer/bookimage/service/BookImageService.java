package com.hazer.bookimage.service;

import com.hazer.bookimage.BookImagePlugin;
import com.hazer.bookimage.util.ImageRenderUtil;
import com.hazer.bookimage.util.JsonPayloadUtil;
import com.hazer.bookimage.util.UrlUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts URL text in books into rendered components.
 */
public class BookImageService {

    private static final Pattern URL_PATTERN = Pattern.compile("https?://\\S+");
    private static final int CONNECT_TIMEOUT_MS = (int) Duration.ofSeconds(5).toMillis();
    private static final int READ_TIMEOUT_MS = (int) Duration.ofSeconds(10).toMillis();

    private final BookImagePlugin plugin;

    public BookImageService(BookImagePlugin plugin) {
        this.plugin = plugin;
    }

    public ConversionResult convertBookMeta(BookMeta sourceMeta) {
        List<Component> converted = new ArrayList<>();
        int convertedPages = 0;

        for (Component page : sourceMeta.pages()) {
            String text = PlainTextComponentSerializer.plainText().serialize(page);
            Optional<String> firstUrl = extractFirstUrl(text);

            if (firstUrl.isEmpty()) {
                converted.add(page);
                continue;
            }

            String url = firstUrl.get();
            try {
                Component rendered = renderUrlToPage(url);
                converted.add(rendered);
                convertedPages++;
            } catch (IOException exception) {
                converted.add(Component.text("§cОшибка загрузки: " + exception.getMessage()));
            }
        }

        BookMeta clone = (BookMeta) sourceMeta.clone();
        clone.pages(converted);
        return new ConversionResult(clone, convertedPages);
    }

    public void convertBookInHandAsync(Player player, Runnable onDone) {
        if (!(player.getInventory().getItemInMainHand().getItemMeta() instanceof BookMeta meta)) {
            player.sendMessage("§cВозьмите книгу в главную руку.");
            return;
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            ConversionResult result = convertBookMeta(meta);
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                var item = player.getInventory().getItemInMainHand();
                item.setItemMeta(result.bookMeta());
                if (result.convertedPages() > 0) {
                    player.sendMessage("§aГотово: страниц с изображениями: " + result.convertedPages());
                } else {
                    player.sendMessage("§eURL изображений не найдено.");
                }
                if (onDone != null) {
                    onDone.run();
                }
            });
        });
    }

    /**
     * Create a new written book from URL and consume one writable book (book & quill) from player's inventory.
     */
    public void giveImageBookFromUrlAsync(Player player, String url) {
        if (!UrlUtils.isValidImageUrl(url)) {
            player.sendMessage("§cНедействительный URL. Разрешены только http/https PNG/JPEG ссылки.");
            return;
        }

        if (!player.getInventory().contains(Material.WRITABLE_BOOK)) {
            player.sendMessage("§cУ вас нет книги с пером (WRITABLE_BOOK) в инвентаре.");
            return;
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Component rendered = renderUrlToPage(url);

                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (!player.isOnline()) {
                        return;
                    }

                    ItemStack writableBook = new ItemStack(Material.WRITABLE_BOOK, 1);
                    player.getInventory().removeItem(writableBook);

                    ItemStack writtenBook = new ItemStack(Material.WRITTEN_BOOK);
                    BookMeta meta = (BookMeta) writtenBook.getItemMeta();
                    if (meta == null) {
                        player.sendMessage("§cОшибка: не удалось создать книгу.");
                        return;
                    }

                    meta.author(Component.text("Hazer_2_0"));
                    meta.title(Component.text("Image Book"));
                    meta.pages(List.of(rendered));
                    writtenBook.setItemMeta(meta);

                    var leftovers = player.getInventory().addItem(writtenBook);
                    if (!leftovers.isEmpty()) {
                        leftovers.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
                    }

                    player.sendMessage("§aКнига с картинкой создана. Одна книга с пером была использована.");
                });
            } catch (IOException ex) {
                Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage("§cОшибка загрузки изображения: " + ex.getMessage()));
            }
        });
    }

    private Component renderUrlToPage(String url) throws IOException {
        if (!UrlUtils.isValidImageUrl(url)) {
            throw new IOException("Недействительный URL изображения: " + url);
        }

        BufferedImage image = downloadImage(url);
        if (image == null) {
            throw new IOException("Не удалось прочитать изображение по URL.");
        }

        BufferedImage scaled = ImageRenderUtil.scaleTo128(image);
        String base64Meta = JsonPayloadUtil.encodeSourceUrl(url);
        return ImageRenderUtil.renderToComponent(scaled, base64Meta);
    }

    private Optional<String> extractFirstUrl(String content) {
        Matcher matcher = URL_PATTERN.matcher(content);
        if (matcher.find()) {
            return Optional.of(matcher.group());
        }
        return Optional.empty();
    }

    private BufferedImage downloadImage(String imageUrl) throws IOException {
        URL url = URI.create(imageUrl).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(true);
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.setRequestProperty("User-Agent", "BookImagePlugin/1.0");

        String contentType = connection.getContentType();
        if (contentType != null) {
            String lower = contentType.toLowerCase();
            if (!lower.contains("png") && !lower.contains("jpeg") && !lower.contains("jpg")) {
                throw new IOException("Поддерживаются только PNG/JPEG. Content-Type=" + contentType);
            }
        }

        try (var stream = connection.getInputStream()) {
            return ImageIO.read(stream);
        }
    }

    public record ConversionResult(BookMeta bookMeta, int convertedPages) {
    }
}
