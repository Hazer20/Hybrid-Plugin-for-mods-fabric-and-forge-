package com.hazer.bookimage.service;

import com.hazer.bookimage.BookImagePlugin;
import com.hazer.bookimage.util.ImageRenderUtil;
import com.hazer.bookimage.util.JsonPayloadUtil;
import com.hazer.bookimage.util.UrlUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
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
            if (!UrlUtils.isValidImageUrl(url)) {
                converted.add(Component.text("§cОшибка: недействительный URL изображения: " + url));
                continue;
            }

            try {
                BufferedImage image = downloadImage(url);
                if (image == null) {
                    converted.add(Component.text("§cОшибка: не удалось прочитать изображение по URL."));
                    continue;
                }

                BufferedImage scaled = ImageRenderUtil.scaleTo128(image);
                String base64Meta = JsonPayloadUtil.encodeSourceUrl(url);
                Component rendered = ImageRenderUtil.renderToComponent(scaled, base64Meta);
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
